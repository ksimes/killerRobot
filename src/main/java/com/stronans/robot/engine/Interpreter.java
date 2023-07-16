package com.stronans.robot.engine;

import com.stronans.robot.Settings;
import com.stronans.robot.core.*;
import com.stronans.robot.fileprocessing.ProcessFile;
import com.stronans.robot.engine.exceptions.QuitException;
import com.stronans.robot.engine.exceptions.StackEmptyException;
import com.stronans.robot.engine.exceptions.UnrecognisedTokenException;
import com.stronans.sensors.Sensors;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.stronans.robot.core.OpCode.quitOut;

/**
 * Handles the processing of Tokens in the input stream to produce actions.
 * Created by S.King on 07/02/2015.
 */
@Slf4j
public class Interpreter {
    private final BufferedInputStream characterStream;
    private final CoreStack dataStack = new CoreStack();
    private final CoreStack returnStack = new CoreStack();
    private long registerA, registerB, registerC;
    private final Settings settings;
    private final Compiler compileWord = new Compiler();
    private RunningMode runMode = RunningMode.interpret;
    private final Token token;
    private final OutputOpcodes outputOpcodes = new OutputOpcodes();
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
                        Common.outputln("Word: [" + token.asText() + "]");
                    }

                    if ((runMode == RunningMode.interpret) || word.isImmediate()) {
                        if (log.isTraceEnabled()) {
                            log.trace("Executing Word : [" + token.asText() + "]");
                        }

                        if (settings.isVerbose()) {
                            Common.outputln("Executing: [" + token.asText() + "]");
                        }
                        runMode = executeWord(word, runMode);
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
                            if (log.isTraceEnabled()) {
                                log.trace("Pushing number : [" + token.asText() + "]");
                            }

                            dataStack.push(aNumber.get());
                        } else {
                            compileWord.addNumber(aNumber.get());
                        }

                    } else {
                        if (runMode == RunningMode.compile) {
                            Optional<OpCode> opCode = token.asOpCode();
                            if (opCode.isPresent()) {
                                if (settings.isVerbose()) {
                                    Common.outputln("Have opcode : [" + opCode.get() + "]");
                                }

                                if (log.isTraceEnabled()) {
                                    log.trace("compiling opcode : [" + opCode.get() + "]");
                                }

                                compileWord.addOpCode(opCode.get());
                            } else {
                                throw new UnrecognisedTokenException(token.asText());
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace("Word not found : [" + token.asText() + "] - token");
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
        registerC = 0;
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
    private void readStreamToCharacter(BufferedInputStream fis, char delimiter) throws IOException {
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

    private void tracePoint(int codePointer, String data)
    {
        if (log.isTraceEnabled()) {
            log.trace("CodePoint " + codePointer + " : " + data);
        }

        if (settings.isVerbose()) {
            Common.outputln("CodePoint " + codePointer + " : " + data);
        }
    }

    private RunningMode executeWord(Word word, RunningMode runMode) throws StackEmptyException, IOException, QuitException {

        for (int codePointer = 0; codePointer < word.getCode().size(); codePointer++) {

            MemoryEntry command = word.getCode().get(codePointer);

            switch (command.getType()) {
                case Word -> {
                    Word wordExecuting = command.getWord();
                    tracePoint(codePointer, "Executing Word : " + wordExecuting.getName());
                    runMode = executeWord(wordExecuting, runMode);
                    lastWord = wordExecuting;
                }

                case Number -> {
                    long number = command.getNumber();
                    tracePoint(codePointer, "Push number : " + number);
                    dataStack.push(number);
                }

                case Address -> {
                    int address = command.getAddress();
                    tracePoint(codePointer, "Jump to word address : " + address);
                    if (address > -2) {
                        codePointer = address;
                    }
                }

                case StringPointer -> {
                    String toEmit = settings.getStringLibrary().get(command.getStringKey());
                    tracePoint(codePointer, "Emit string [" + toEmit + "]");
                    Common.emitString(toEmit);
                }

                case OpCode -> {
                    switch (command.getOperation()) {
                        case toInterpretMode -> {
                            if (runMode == RunningMode.compile) {
                                tracePoint(codePointer, "End of Word \n");
                                settings.getDictionary().addWord(compileWord.getAsWordListing());
                            }
                            runMode = RunningMode.interpret;
                        }

                        case toCompileMode -> {
                            if (runMode == RunningMode.interpret) {
                                // Get the next token and this is the new word.
                                token.getToken();
                                if (settings.isVerbose()) {
                                    Common.outputln("Compiling word: [" + token.asText() + "]");
                                }
                                compileWord.startWord(token.asText());
                            }
                            runMode = RunningMode.compile;
                            if (settings.isVerbose()) {
                                Common.outputln("Run mode switch to compile");
                            }
                        }

                        case ifTest -> {
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                // If there is a non-zero values on TOS then move past the THEN address
                                // pushed into the next address.
                                compileWord.addOpCode(OpCode.popA);
                                compileWord.addOpCode(OpCode.jumpANEq0);
                                compileWord.addAddress(-1);
                                returnStack.push(compileWord.getCodePointer());  // addressR
                            }
                        }

                        case thenJump -> {
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                long jumpPointer = returnStack.pop();
                                compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                            }
                        }

                        case elseJump -> {
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                long jumpPointer = returnStack.pop();
                                compileWord.addAddress(-1);
                                returnStack.push(compileWord.getCodePointer());
                                compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                            }
                        }

                        case doStart -> {           // Start of a do loop, pushes top two data stack onto return stack + address
                            switch (runMode) {
                                case interpret -> {
                                    registerA = dataStack.pop();        // popA
                                    registerB = dataStack.pop();        // popB

                                    // Note that we decrement the delimiter value here by one as the loop test is
                                    // at the end of the loop and not the start
                                    returnStack.push(--registerB);      // decB, pushRB
                                    returnStack.push(registerA);        // pushRA
                                }

                                case compile -> {
//                                    compileWord.addOpCode(OpCode.popA);
//                                    compileWord.addOpCode(OpCode.popB);
//                                    compileWord.addOpCode(OpCode.decB);
//                                    compileWord.addOpCode(OpCode.pushRB);
//                                    compileWord.addOpCode(OpCode.pushRA);
                                    compileWord.addOpCode(OpCode.doStart);
                                    returnStack.push(compileWord.getCodePointer());
                                }
                            }
                        }

                        case loop -> {
                            switch (runMode) {
                                case interpret -> {
                                    registerA = returnStack.pop();      // popA
                                    registerB = returnStack.pop();      // popB
                                    if (registerA == registerB) {       // jumpEqAB
                                        codePointer++;
                                    } else {
                                        registerA++;                    // incA
                                        returnStack.push(registerB);    // pushRB
                                        returnStack.push(registerA);    // pushRA
                                    }
                                }
                                case compile -> {
                                    compileWord.addOpCode(OpCode.loop);
                                    compileWord.addAddress(returnStack.pop());
                                }
                            }
                        }

                        case plusLoop -> {
                            switch (runMode) {
                                case interpret -> {
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
                                }

                                case compile -> {
                                    compileWord.addOpCode(OpCode.plusLoop);
                                    compileWord.addAddress(returnStack.pop());
                                }
                            }
                        }

                        case begin -> {
                            // Does nothing in interpret mode.
                            if (runMode == RunningMode.compile) {
                                returnStack.push(compileWord.getCodePointer());
                            }
                        }

                        case again -> {
                            switch (runMode) {
                                case interpret:
                                    break;

                                case compile:
                                    compileWord.addAddress(returnStack.pop());
                                    break;
                            }
                        }

                        case quit -> {
                            switch (runMode) {
                                case interpret -> throw new QuitException();
                                case compile -> compileWord.addOpCode(OpCode.quit);
                            }
                        }

                        case jumpEqAB -> {     // If register A == B then skip next memory location
                            if (registerA == registerB) {
                                codePointer++;
                            }
                        }

                        // Jump if A Not Equal to zero
                        case jumpANEq0 -> {     // If register A != 0 then skip next memory location
                            if (registerA != 0) {
                                codePointer++;
                            }
                        }

                        case buildVariable -> {
                            // Get the next token and this is the new variable word.
                            token.getToken();
                            compileWord.startWord(token.asText());
                            if (settings.isVerbose()) {
                                Common.outputln("Build variable: [" + token.asText() + "]");
                            }
                            compileWord.addOpCode(quitOut);
                            compileWord.addNumber(0L);
                            settings.getDictionary().addWord(compileWord.getAsWordListing());
                        }

                        case quitOut -> codePointer = word.getCode().size();

                        case storeVariable -> {
                            if (lastWord != null) {
                                MemoryEntry entry = lastWord.getCode().get(0);
                                if (entry.getType() == MemoryType.OpCode && entry.getOperation() == quitOut) {
                                    MemoryEntry me = new MemoryEntry(registerA);
                                    lastWord.getCode().set(1, me);
                                }
                            }
                        }

                        case fetchVariable -> {
                            if (lastWord != null) {
                                MemoryEntry entry = lastWord.getCode().get(0);
                                if (entry.getType() == MemoryType.OpCode && entry.getOperation() == quitOut) {
                                    entry = lastWord.getCode().get(1);
                                    dataStack.push(entry.getNumber());
                                }
                            }
                        }

                        case buildConstant -> {
                            // Get the next token and this is the new variable word.
                            token.getToken();
                            compileWord.startWord(token.asText());
                            if (settings.isVerbose()) {
                                Common.outputln("Build Constant: [" + token.asText() + "]");
                            }
                            compileWord.addNumber(registerA);
                            settings.getDictionary().addWord(compileWord.getAsWordListing());
                        }

                        default -> coreOperation(command.getOperation());
                    }
                }
            }
        }

        return runMode;
    }

    private void coreOperation(OpCode code) throws StackEmptyException, IOException {
        switch (code) {
            case pushA:     // Push contexts of A register to stack
                dataStack.push(registerA);
                log.trace("Push to data stack registerA: " + registerA);
                break;

            case pushB:     // Push contents of B register to stack
                dataStack.push(registerB);
                log.trace("Push to data stack registerB: " + registerB);
                break;

            case pushC:     // Push contents of C register to stack
                dataStack.push(registerC);
                log.trace("Push to data stack registerC: " + registerC);
                break;

            case popA:      // Pop contexts of top of stack to reg A
                registerA = dataStack.pop();
                log.trace("Pop to registerA: " + registerA);
                break;

            case popB:      // Pop contexts of top of stack to reg B
                registerB = dataStack.pop();
                log.trace("Pop to registerB: " + registerB);
                break;

            case popC:      // Pop contexts of top of stack to reg B
                registerC = dataStack.pop();
                log.trace("Pop to registerC: " + registerC);
                break;

            case incA:
                registerA++;
                log.trace("increment registerA to: " + registerA);
                break;

            case decA:
                registerA--;
                log.trace("decrement registerA to: " + registerA);
                break;

            case incB:
                registerB++;
                log.trace("increment registerB to: " + registerB);
                break;

            case decB:
                registerB--;
                log.trace("decrement registerB to: " + registerB);
                break;

            case addAB:
                registerA += registerB;
                log.trace("Add registerB to registerA: " + registerA);
                break;

            case subAB:
                registerA -= registerB;
                log.trace("Subtract registerB from registerA: " + registerA);
                break;

            case mulAB:
                registerA *= registerB;
                log.trace("Multiple registerB and registerA: " + registerA);
                break;

            case divAB:
                registerA /= registerB;
                log.trace("Divide registerA with registerB: " + registerA);
                break;

            // Logic grouping
            case equalAB, lessAB, greaterAB, andAB, orAB:
                logic(code);
                break;

            case printA:
                log.trace("emit number [" + registerA + "]");
                Common.emitNumber(registerA);
                break;

            case printB:
                log.trace("emit number [" + registerB + "]");
                Common.emitNumber(registerB);
                break;

            case emitA:
                log.trace("emit Character [" + registerA + "]");
                Common.emitChar((int) registerA);
                break;

            case emitB:
                log.trace("emit Character [" + registerB + "]");
                Common.emitChar((int) registerB);
                break;

            case processComment:
                readStreamToCharacter(characterStream, ')');
                break;

            case processComment2:
                readStreamToCharacter(characterStream, '\n');
                break;

            case processString:
                String data = Common.processString(characterStream, '"');
                switch (runMode) {
                    case interpret -> Common.emitString(data);

                    case compile -> {
                        String stringKey = settings.getStringLibrary().add(data);
                        compileWord.addStringKey(stringKey);
                        log.trace("process String [" + data + "] compile to :" + stringKey);
                    }
                }
                break;

            case dumpDictionary:
                for (String word : settings.getDictionary().getWords()) {
                    Common.emitString(word);
                    Common.emitChar('\n');
                }
                break;

            case delay:
                log.trace("Delay program for [" + registerA + "] Milliseconds");
                // block the current thread for registerA duration in Milliseconds
                try {
                    TimeUnit.MILLISECONDS.sleep(registerA);
                } catch (InterruptedException e) {
                    log.warn(registerA + " Millisecond sleep interrupted: " + e.getMessage(), e);
                }
                break;

            case makeImmediate:
                settings.getDictionary().makeLastWordImmediate();
                log.trace("Make last word immediate");
                break;

            case load:
                readStreamToCharacter(characterStream, '"');
                String filename = Common.processString(characterStream, '"');
                log.trace("Load file : " + filename);
                ProcessFile pf = new ProcessFile(filename, settings);
                pf.process();
                break;

            case addressR:
                returnStack.push(compileWord.getCodePointer());
                break;

            case pushAddressR:
                break;

            case distance:      // Input data from sensor
                long distance = Sensors.getSensorData(registerA);
                dataStack.push(distance);
                log.trace("measure sensor [" + registerA + "] value = {" + distance + "}");
                break;

            case Carriage:      // Tracks, wheels or legs all together
                outputOpcodes.execute(code, registerA, registerB);
                break;

            case step:      // Take a step using legs. TBD
                break;

            default:
                log.trace("Unused Opcode - " + code.name());
                break;
        }
    }

    private void logic(OpCode code) {
        switch (code) {
            case equalAB -> {       // Is reg A equal to Reg B
                log.trace("Equals RegA & RegB : " + registerA + " = " + registerB);
                if (registerA == registerB)
                    registerA = 1;
                else
                    registerA = 0;
            }

            case lessAB -> {        // Is reg A less than Reg B
                log.trace("Less RegA & RegB : " + registerA + " < " + registerB);
                if (registerA < registerB)
                    registerA = 1;
                else
                    registerA = 0;
            }

            case greaterAB -> {     // Is reg A greater than Reg B
                log.trace("Greater RegA & RegB : " + registerA + " > " + registerB);
                if (registerA > registerB)
                    registerA = 1;
                else
                    registerA = 0;
            }

            case andAB -> {     // Is reg A and Reg B true
                log.trace("And RegA & RegB : " + registerA + " && " + registerB);
                if (registerA != 0 && registerB != 0)
                    registerA = 1;
                else
                    registerA = 0;
            }

            case orAB -> {     // Is reg A or Reg B true
                log.trace("Or RegA & RegB : " + registerA + " || " + registerB);
                if (registerA != 0 || registerB != 0)
                    registerA = 1;
                else
                    registerA = 0;
            }
        }
    }
}
