package com.stronans.robot.interpreter;

import com.stronans.robot.Settings;
import com.stronans.robot.core.*;
import com.stronans.robot.fileprocessing.ProcessFile;
import com.stronans.robot.interpreter.exceptions.QuitException;
import com.stronans.robot.interpreter.exceptions.StackEmptyException;
import com.stronans.robot.interpreter.exceptions.UnrecognisedTokenException;
import com.stronans.sensors.Sensors;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Optional;

import static com.stronans.robot.core.OpCode.quitOut;

/**
 * Handles the processing of Tokens in the input stream to produce actions.
 * Created by S.King on 07/02/2015.
 */
public class Interpreter {
    private static final Logger logger = Logger.getLogger(Compiler.class);

    private BufferedInputStream characterStream;
    private CoreStack dataStack = new CoreStack();
    private CoreStack returnStack = new CoreStack();
    private long registerA, registerB;
    private Settings settings;
    private Compiler compileWord = new Compiler();
    private RunningMode runMode = RunningMode.interpret;
    private Token token;
    private SpecialOpcodes specialOpCodes = new SpecialOpcodes();
    private Word lastWord = null;

    public Interpreter(Settings settings, BufferedInputStream characterStream) {
        this.settings = settings;
        this.characterStream = characterStream;
        this.token = new Token(characterStream, settings.getDictionary());
    }

    public void processStream() throws IOException {
        runMode = RunningMode.interpret;

        try {
            while (token.getToken()) {

                Optional<Word> aWord = token.asWord();
                if (aWord.isPresent()) {
                    Word word = aWord.get();

                    if (settings.isVerbose()) {
                        Common.outputln("Have word : [" + token.asText() + "]");
                    }

                    if ((runMode == RunningMode.interpret) || word.isImmediate()) {
                        runMode = execute(word, runMode);
                    } else {
                        compileWord.addWord(word);
                    }
                    lastWord = word;
                } else {
                    Optional<Long> aNumber = token.asNumber();
                    if (aNumber.isPresent()) {

                        if (settings.isVerbose()) {
                            Common.outputln("Have number : [" + token.asText() + "]");
                        }

                        if (runMode == RunningMode.interpret) {
                            dataStack.push(aNumber.get());
                        } else {
                            compileWord.addNumber(aNumber.get());
                        }

                    } else {
                        if (runMode == RunningMode.compile) {
                            Optional<OpCode> opCode = token.asOpCode();
                            if (opCode.isPresent()) {
                                compileWord.addOpCode(opCode.get());
                            } else {
                                throw new UnrecognisedTokenException(token.asText());
                            }
                        } else {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Word not found : [" + token.asText() + "] - token");
                            }
                            Common.outputln(token.asText() + " ?");
                        }
                    }
                }
            }
        } catch (UnrecognisedTokenException uwe) {
            Common.outputln(uwe.getMessage());
            abort();
        } catch (StackEmptyException see) {
            Common.outputln(see.getMessage());
        } catch (QuitException q) {
            Common.outputln("Quit");
        }
    }

    private void abort() {
        token.clear();
        dataStack.clear();
        returnStack.clear();
        registerA = 0;
        registerB = 0;
    }

    /**
     * Steps over characters until the delimiting character is found.
     * Used in processing comments and moving from the end of words to and special delimiters
     * before staring processing, such as LOAD    "
     *
     * @param fis       file input stream to read
     * @param delimiter character to stop reading at. Read position over to next character.
     * @throws IOException
     */
    private void readToCharacter(BufferedInputStream fis, char delimiter) throws IOException {
        boolean finished;
        int content;
        // read the input stream until we find the delimiting character
        // or hit the end of the stream and then return
        do {
            content = fis.read();

            finished = content == -1;

            if (!finished && (char) content == delimiter)
                finished = true;
        }
        while (!finished);
    }

    private RunningMode execute(Word word, RunningMode runMode) throws StackEmptyException, IOException, QuitException {

        for (int codePointer = 0; codePointer < word.getCode().size(); codePointer++) {

            MemoryEntry command = word.getCode().get(codePointer);

            switch (command.getType()) {
                case Word:
                    runMode = execute(command.getWord(), runMode);
                    lastWord = command.getWord();
                    break;

                case Number:
                    dataStack.push(command.getNumber());
                    break;

                case Address:
                    if (command.getAddress() > -2) {
                        codePointer = command.getAddress();
                    }
                    break;

                case StringPointer:
                    Common.emitString(settings.getStringLibrary().get(command.getStringKey()));
                    break;

                case OpCode:
                    switch (command.getOperation()) {
                        case toInterpretMode:
                            if (runMode == RunningMode.compile) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("End of Word \n");
                                }
                                settings.getDictionary().addWord(compileWord.getAsWordListing());
                            }
                            runMode = RunningMode.interpret;
                            break;

                        case toCompileMode:
                            if (runMode == RunningMode.interpret) {
                                // Get the next token and this is the new word.
                                token.getToken();
                                compileWord.startWord(token.asText());
                            }
                            runMode = RunningMode.compile;
                            break;

                        case ifTest:
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                // If there is a non-zero values on TOS then move past the THEN address
                                // pushed into the next address.
                                compileWord.addOpCode(OpCode.popA);
                                compileWord.addOpCode(OpCode.jumpANEq0);
                                compileWord.addAddress(-1);
                                returnStack.push(compileWord.getCodePointer());  // addressR
                            }
                            break;

                        case thenJump:
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                long jumpPointer = returnStack.pop();
                                compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                            }
                            break;

                        case elseJump:
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                long jumpPointer = returnStack.pop();
                                compileWord.addAddress(-1);
                                returnStack.push(compileWord.getCodePointer());
                                compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                            }
                            break;

                        case doStart:           // Start of a do loop, pushes top two data stack onto return stack + address
                            switch (runMode) {
                                case interpret:
                                    registerA = dataStack.pop();        // popA
                                    registerB = dataStack.pop();        // popB
                                    // Note that we decrement the delimiter value here by one as the loop test is
                                    // at the end of the loop and not the start
                                    returnStack.push(--registerB);      // decB, pushRB
                                    returnStack.push(registerA);        // pushRA
                                    break;

                                case compile:
//                                    compileWord.addOpCode(OpCode.popA);
//                                    compileWord.addOpCode(OpCode.popB);
//                                    compileWord.addOpCode(OpCode.decB);
//                                    compileWord.addOpCode(OpCode.pushRB);
//                                    compileWord.addOpCode(OpCode.pushRA);
                                    compileWord.addOpCode(OpCode.doStart);
                                    returnStack.push(compileWord.getCodePointer());
                                    break;
                            }
                            break;

                        case loop:
                            switch (runMode) {
                                case interpret:
                                    registerA = returnStack.pop();      // popA
                                    registerB = returnStack.pop();      // popB

                                    if (registerA == registerB) {       // jumpEqAB
                                        codePointer++;
                                    } else {
                                        registerA++;                    // incA
                                        returnStack.push(registerB);    // pushRB
                                        returnStack.push(registerA);    // pushRA
                                    }
                                    break;

                                case compile:
                                    compileWord.addOpCode(OpCode.loop);
                                    compileWord.addAddress(returnStack.pop());
                                    break;
                            }
                            break;

                        case plusLoop:
                            switch (runMode) {
                                case interpret:
                                    registerA = returnStack.pop();      // popA
                                    registerB = returnStack.pop();      // popB

                                    if (registerA == registerB) {       // jumpEqAB
                                        codePointer++;
                                    } else {
                                        returnStack.push(registerB);    // pushRB

                                        registerB = dataStack.pop();    // popB
                                        registerA += registerB;         // addAB

                                        returnStack.push(registerA);    // pushRA
                                    }
                                    break;

                                case compile:
                                    compileWord.addOpCode(OpCode.plusLoop);
                                    compileWord.addAddress(returnStack.pop());
                                    break;
                            }
                            break;

                        case begin:
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                returnStack.push(compileWord.getCodePointer());
                            }
                            break;

                        case again:
                            switch (runMode) {
                                case interpret:
                                    break;

                                case compile:
                                    compileWord.addAddress(returnStack.pop());
                                    break;
                            }
                            break;


                        case quit:
                            switch (runMode) {
                                case interpret:
                                    throw new QuitException();

                                case compile:
                                    compileWord.addOpCode(OpCode.quit);
                                    break;
                            }
                            break;

                        case jumpEqAB:     // If register A == B then skip next memory location
                            if (registerA == registerB) {
                                codePointer++;
                            }
                            break;

                        // Jump if A Not Equal to zero
                        case jumpANEq0:     // If register A != 0 then skip next memory location
                            if (registerA != 0) {
                                codePointer++;
                            }
                            break;

                        case buildVariable:
                            // Get the next token and this is the new variable word.
                            token.getToken();
                            compileWord.startWord(token.asText());
                            compileWord.addOpCode(quitOut);
                            compileWord.addNumber(0L);
                            settings.getDictionary().addWord(compileWord.getAsWordListing());
                            break;

                        case quitOut:
                            codePointer = word.getCode().size();
                            break;

                        case storeVariable:
                            if (lastWord != null) {
                                MemoryEntry entry = lastWord.getCode().get(0);
                                if (entry.getType() == MemoryType.OpCode && entry.getOperation() == quitOut) {
                                    MemoryEntry me = new MemoryEntry(registerA);
                                    lastWord.getCode().set(1, me);
                                }
                            }
                            break;

                        case fetchVariable:
                            if (lastWord != null) {
                                MemoryEntry entry = lastWord.getCode().get(0);
                                if (entry.getType() == MemoryType.OpCode && entry.getOperation() == quitOut) {
                                    entry = lastWord.getCode().get(1);
                                    dataStack.push(entry.getNumber());
                                }
                            }
                            break;

                        case buildConstant:
                            // Get the next token and this is the new variable word.
                            token.getToken();
                            compileWord.startWord(token.asText());
                            compileWord.addNumber(registerA);
                            settings.getDictionary().addWord(compileWord.getAsWordListing());
                            break;

                        default:
                            executeOperation(command.getOperation());
                    }
                    break;
            }
        }

        return runMode;
    }

    private void executeOperation(OpCode code) throws StackEmptyException, IOException {
        switch (code) {
            case pushA:     // Push contexts of A register to stack
                dataStack.push(registerA);
                logger.trace("Push to data stack registerA: " + registerA);
                break;

            case pushB:     // Push contents of B register to stack
                dataStack.push(registerB);
                logger.trace("Push to data stack registerB: " + registerB);
                break;

            case popA:      // Pop contexts of top of stack to reg A
                registerA = dataStack.pop();
                logger.trace("Pop to registerA: " + registerA);
                break;

            case popB:      // Pop contexts of top of stack to reg B
                registerB = dataStack.pop();
                logger.trace("Pop to registerB: " + registerB);
                break;

            case incA:
                registerA++;
                logger.trace("increment registerA to: " + registerA);
                break;

            case decA:
                registerA--;
                logger.trace("decrement registerA to: " + registerA);
                break;

            case incB:
                registerB++;
                logger.trace("increment registerB to: " + registerB);
                break;

            case decB:
                registerB--;
                logger.trace("decrement registerB to: " + registerB);
                break;

            case addAB:
                registerA += registerB;
                logger.trace("Add registerB to registerA: " + registerA);
                break;

            case subAB:
                registerA -= registerB;
                logger.trace("Subtract registerB from registerA: " + registerA);
                break;

            case mulAB:
                registerA *= registerB;
                logger.trace("Multiple registerB and registerA: " + registerA);
                break;

            case divAB:
                registerA /= registerB;
                logger.trace("Divide registerA with registerB: " + registerA);
                break;

            // Logic grouping
            case equalAB:
            case lessAB:
            case greaterAB:
                logic(code);
                break;

            case printA:
                Common.emitNumber(registerA);
                break;

            case printB:
                Common.emitNumber(registerB);
                break;

            case emitA:
                Common.emitChar((int) registerA);
                break;

            case emitB:
                Common.emitChar((int) registerB);
                break;

            case processComment:
                readToCharacter(characterStream, ')');
                logger.trace("processComment");
                break;

            case processComment2:
                readToCharacter(characterStream, '\n');
                logger.trace("processComment2");
                break;

            case processString:
                String data = Common.processString(characterStream, '"');
                switch (runMode) {
                    case interpret:
                        Common.emitString(data);
                        break;

                    case compile:
                        String stringKey = settings.getStringLibrary().add(data);
                        compileWord.addStringKey(stringKey);
                        logger.trace("process String [" + data + "] compile to :" + stringKey);
                        break;
                }
                break;

            case dumpDictionary:
                for (String word : settings.getDictionary().getWords()) {
                    Common.emitString(word);
                    Common.emitChar('\n');
                }
                break;

            case makeImmediate:
                settings.getDictionary().makeLastWordImmediate();
                logger.trace("Make last word immediate");
                break;

            case step:
                break;

            case distance:
                dataStack.push(Sensors.getSensorData(registerA));
                logger.trace("measure sensor " + registerA);
                break;

            case load:
                readToCharacter(characterStream, '"');
                String filename = Common.processString(characterStream, '"');
                logger.trace("Load file : " + filename);
                ProcessFile pf = new ProcessFile(filename, settings);
                pf.process();
                break;

            case addressR:
                returnStack.push(compileWord.getCodePointer());
                break;

            case pushAddressR:
                break;

            default:
                specialOpCodes.execute(code, registerA);
                break;
        }
    }

    private void logic(OpCode code) {
        switch (code) {
            case equalAB:       // Is reg A equal to Reg B
                if (registerA == registerB)
                    registerA = 1;
                else
                    registerA = 0;
                break;

            case lessAB:        // Is reg A less than Reg B
                if (registerA < registerB)
                    registerA = 1;
                else
                    registerA = 0;
                break;

            case greaterAB:     // Is reg A greater than Reg B
                if (registerA > registerB)
                    registerA = 1;
                else
                    registerA = 0;
                break;
        }
    }
}
