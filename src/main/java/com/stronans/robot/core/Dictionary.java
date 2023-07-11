package com.stronans.robot.core;

import java.util.*;

/**
 * Holds the Word dictionary for the FORTH controller language.
 * Created by S.King on 07/02/2015.
 */
public final class Dictionary {

    private final Map<String, Word> listing = new HashMap<>(200);
    private Word lastWordAdded = null;

    public Dictionary() {
        List<MemoryEntry> compiledCode = new ArrayList<>();

        // Add in the default words to the dictionary

        // Add in comment "("
        compiledCode.add(new MemoryEntry(OpCode.processComment));
        addWord(new Word("(", compiledCode, true));

        compiledCode = new ArrayList<>();
        // Add in compile ":"
        compiledCode.add(new MemoryEntry(OpCode.toCompileMode));
        addWord(new Word(":", compiledCode, true));

        compiledCode = new ArrayList<>();
        // Add in finish compile ";"
        compiledCode.add(new MemoryEntry(OpCode.toInterpretMode));
        addWord(new Word(";", compiledCode, true));
    }

    public void addWord(Word word) {
        listing.put(word.getName(), word);

        // Save a reference to the last word in case we wish to make it immediate.
        lastWordAdded = word;
    }

    public List<String> getWords()
    {
        List<String> words = new ArrayList<>(listing.keySet());

        Collections.sort(words);

        return words;
    }

    public void makeLastWordImmediate() {
        lastWordAdded.makeImmediate();
    }

    public Word lookup(String token) {
        return listing.get(token.toLowerCase());
    }
}
