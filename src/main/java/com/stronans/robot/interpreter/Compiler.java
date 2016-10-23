package com.stronans.robot.interpreter;

import com.stronans.robot.core.MemoryEntry;
import com.stronans.robot.core.OpCode;
import com.stronans.robot.core.RunningMode;
import com.stronans.robot.core.Word;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes a set of tokens and numbers and compiles them into a Word for later interpretation.
 * <p/>
 * Created by S.King on 07/02/2015.
 */
class Compiler {
    private static final Logger logger = Logger.getLogger(Compiler.class);
    private String name;
    private List<MemoryEntry> compiledCode = null;

    RunningMode startWord(String token) {
        RunningMode result = RunningMode.compile;

        // Clear the new Word buffers and prepare to create a new Word.
        name = token;
        compiledCode = new ArrayList<>();

        if (logger.isTraceEnabled()) {
            logger.trace("Start Word Compile : [" + token + "]");
        }

        return result;
    }

    long getCodePointer() {
        if (logger.isTraceEnabled()) {
            logger.trace("get code Pointer : [" + (compiledCode.size() - 1) + "]");
        }

        return compiledCode.size() - 1;
    }

    Word getAsWordListing() {
        if (logger.isTraceEnabled()) {
            logger.trace("Start of Word dump : " + name);
            int counter = 0;
            for (MemoryEntry me : compiledCode) {
                switch (me.getType()) {
                    case Word:
                        logger.trace("Dump " + counter + ":Word reference - " + me.getWord().getName());
                        break;

                    case Number:
                        logger.trace("Dump " + counter + ":Number - " + me.getNumber());
                        break;

                    case Address:
                        logger.trace("Dump " + counter + ":Address - " + me.getAddress());
                        break;

                    case StringPointer:
                        logger.trace("Dump " + counter + ":String - " + me.getStringKey());
                        break;

                    case OpCode:
                        logger.trace("Dump " + counter + ":OpCode - " + me.getOperation());
                        break;
                }
                counter++;
            }
            logger.trace("End of Word dump : " + name + "\n");
        }

        return new Word(name, compiledCode);
    }

    void addWord(Word word) {
        MemoryEntry me = new MemoryEntry(word);
        compiledCode.add(me);

        if (logger.isTraceEnabled()) {
            logger.trace("Add Word : [" + word.getName() + "] " + (compiledCode.size() - 1));
        }
    }

    void addAddress(long codePointer) {
        MemoryEntry me = new MemoryEntry((int) codePointer);
        compiledCode.add(me);

        if (logger.isTraceEnabled()) {
            logger.trace("Add address : [" + codePointer + "]" + (compiledCode.size() - 1));
        }
    }

    void pokeAddress(long codePointer, long newAddress) {
        if (logger.isTraceEnabled()) {
            logger.trace("Poke address : [cp:" + codePointer + " na:" + newAddress + "]");
        }
        MemoryEntry me = new MemoryEntry((int) newAddress);
        compiledCode.set((int) codePointer, me);
    }

    void addNumber(Long number) {

        MemoryEntry me = new MemoryEntry(number);
        compiledCode.add(me);

        if (logger.isTraceEnabled()) {
            logger.trace("Add Number : [" + number + "]" + (compiledCode.size() - 1));
        }
    }

    void addOpCode(OpCode token) {
        MemoryEntry me = new MemoryEntry(token);
        compiledCode.add(me);

        if (logger.isTraceEnabled()) {
            logger.trace("Add OpCode : [" + token + "]" + (compiledCode.size() - 1));
        }

    }

    void addStringKey(String key) {
        MemoryEntry me = new MemoryEntry(key);
        compiledCode.add(me);

        if (logger.isTraceEnabled()) {
            logger.trace("Add String key : [" + key + "]" + (compiledCode.size() - 1));
        }
    }
}
