package com.stronans.robot.interpreter;

import com.google.common.base.Optional;
import com.stronans.robot.Settings;
import com.stronans.robot.core.*;
import com.stronans.robot.fileprocessing.ProcessFile;
import com.stronans.robot.interpreter.exceptions.QuitException;
import com.stronans.robot.interpreter.exceptions.StackEmptyException;
import com.stronans.robot.interpreter.exceptions.UnrecognisedTokenException;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;

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
                        System.out.println("Have word : [" + token.asText() + "]");
                    }

                    if ((runMode == RunningMode.interpret) || word.isImmediate()) {
                        runMode = execute(word, runMode);
                    } else {
                        compileWord.addWord(word);
                    }
                } else {
                    Optional<Long> aNumber = token.asNumber();
                    if (aNumber.isPresent()) {

                        if (settings.isVerbose()) {
                            System.out.println("Have number : [" + token.asText() + "]");
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
                                compileWord.addOpCode(token.asOpCode().get());
                            } else {
                                throw new UnrecognisedTokenException(token.asText());
                            }
                        } else {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Word not found : [" + token.asText() + "] - token");
                            }
                            System.out.println(token.asText() + " ?");
                        }
                    }
                }
            }
        } catch (UnrecognisedTokenException uwe) {
            System.err.println(uwe.getMessage());
            abort();
        } catch (StackEmptyException see) {
            System.err.println(see.getMessage());
        } catch (QuitException q) {
            System.err.println("Quit");
        }
    }

    private void abort() {
        token.clear();
        dataStack.clear();
        returnStack.clear();
        registerA = 0;
        registerB = 0;
    }

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

    public RunningMode execute(Word word, RunningMode runMode) throws StackEmptyException, IOException, QuitException {

        for (int codePointer = 0; codePointer < word.getCode().size(); codePointer++) {

            MemoryEntry command = word.getCode().get(codePointer);

            switch (command.getType()) {
                case Word:
                    runMode = execute(command.getWord(), runMode);
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
                            switch (runMode) {
                                case interpret:
                                    // No operate in interpret mode.
                                    break;

                                case compile:
                                    // If there is a non-zero values on TOS then move past the THEN address
                                    // pushed into the next address.
                                    compileWord.addOpCode(OpCode.popA);
                                    compileWord.addOpCode(OpCode.jumpANEq0);
                                    compileWord.addAddress(-1);
                                    returnStack.push(compileWord.getCodePointer());  // addressR
                                    break;
                            }
                            break;

                        case thenJump:
                            switch (runMode) {
                                case interpret:
                                    // No operate in interpret mode.
                                    break;

                                case compile:
                                    long jumpPointer = returnStack.pop();
                                    compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                                    break;
                            }
                            break;

                        case elseJump:
                            switch (runMode) {
                                case interpret:
                                    // No operate in interpret mode.
                                    break;

                                case compile:
                                    long jumpPointer = returnStack.pop();
                                    compileWord.addAddress(-1);
                                    returnStack.push(compileWord.getCodePointer());
                                    compileWord.pokeAddress(jumpPointer, compileWord.getCodePointer());
                                    break;
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
                            switch (runMode) {
                                case interpret:
                                    break;

                                case compile:
                                    returnStack.push(compileWord.getCodePointer());
                                    break;
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
                break;

            case pushB:     // Push contents of B register to stack
                dataStack.push(registerB);
                break;

            case popA:      // Pop contexts of top of stack to reg A
                registerA = dataStack.pop();
                break;

            case popB:      // Pop contexts of top of stack to reg B
                registerB = dataStack.pop();
                break;

            case incA:
                registerA++;
                break;

            case decA:
                registerA--;
                break;

            case incB:
                registerB++;
                break;

            case decB:
                registerB--;
                break;

            case addAB:
                registerA += registerB;
                break;

            case subAB:
                registerA -= registerB;
                break;

            case mulAB:
                registerA *= registerB;
                break;

            case divAB:
                registerA /= registerB;
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
                break;

            case processComment2:
                readToCharacter(characterStream, '\n');
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
                break;

            case step:
                break;

            case distance:
                break;

            case load:
                readToCharacter(characterStream, '"');
                String filename = Common.processString(characterStream, '"');
                ProcessFile pf = new ProcessFile(filename, settings);
                pf.process();
                break;

            case addressR:
                returnStack.push(compileWord.getCodePointer());
                break;

            case pushAddressR:
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
