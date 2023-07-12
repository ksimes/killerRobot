package com.stronans.robot.engine;

import com.stronans.robot.core.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes a set of tokens and numbers and compiles them into a Word for later interpretation.
 * <p/>
 * Created by S.King on 07/02/2015.
 */
@Slf4j
class Compiler {
    private String name;
    private List<MemoryEntry> compiledCode = null;

    RunningMode startWord(String token) {
        RunningMode result = RunningMode.compile;

        // Clear the new Word buffers and prepare to create a new Word.
        name = token;
        compiledCode = new ArrayList<>();

        if (log.isTraceEnabled()) {
            log.trace("Start Word Compile : [" + token + "]");
        }

        return result;
    }

    long getCodePointer() {
        if (log.isTraceEnabled()) {
            log.trace("get code Pointer : [" + (compiledCode.size() - 1) + "]");
        }

        return compiledCode.size() - 1;
    }

    Word getAsWordListing() {
        if (log.isTraceEnabled()) {
            log.trace("Start of Word dump : " + name);
            int counter = 0;
            for (MemoryEntry me : compiledCode) {
                switch (me.getType()) {
                    case Word -> log.trace("Dump " + counter + ":Word reference - " + me.getWord().getName());
                    case Number -> log.trace("Dump " + counter + ":Number - " + me.getNumber());
                    case Address -> log.trace("Dump " + counter + ":Address - " + me.getAddress());
                    case StringPointer -> log.trace("Dump " + counter + ":String - " + me.getStringKey());
                    case OpCode -> log.trace("Dump " + counter + ":OpCode - " + me.getOperation());
                }
                counter++;
            }
            log.trace("End of Word dump : " + name + "\n");
        }

        return new Word(name, compiledCode);
    }

    void addWord(Word word) {
        MemoryEntry me = new MemoryEntry(word);
        compiledCode.add(me);

        if (log.isTraceEnabled()) {
            log.trace("Add Word : [" + word.getName() + "] " + (compiledCode.size() - 1));
        }
    }

    void addAddress(long codePointer) {
        MemoryEntry me = new MemoryEntry((int) codePointer);
        compiledCode.add(me);

        if (log.isTraceEnabled()) {
            log.trace("Add address : [" + codePointer + "]" + (compiledCode.size() - 1));
        }
    }

    void pokeAddress(long codePointer, long newAddress) {
        if (log.isTraceEnabled()) {
            log.trace("Poke address : [cp:" + codePointer + " na:" + newAddress + "]");
        }
        MemoryEntry me = new MemoryEntry((int) newAddress);
        compiledCode.set((int) codePointer, me);
    }

    void addNumber(Long number) {

        MemoryEntry me = new MemoryEntry(number);
        compiledCode.add(me);

        if (log.isTraceEnabled()) {
            log.trace("Add Number : [" + number + "]" + (compiledCode.size() - 1));
        }
    }

    void addOpCode(OpCode token) {
        MemoryEntry me = new MemoryEntry(token);
        compiledCode.add(me);

        if (log.isTraceEnabled()) {
            log.trace("Add OpCode : [" + token + "]" + (compiledCode.size() - 1));
        }

    }

    void addStringKey(String key) {
        MemoryEntry me = new MemoryEntry(key);
        compiledCode.add(me);

        if (log.isTraceEnabled()) {
            log.trace("Add String key : [" + key + "]" + (compiledCode.size() - 1));
        }
    }
}
