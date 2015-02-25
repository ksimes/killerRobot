package com.stronans.robot.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a FORTH word. The name is stored in Lower case but the dictionary search is case insensitive.
 *
 * Created by S.King on 07/02/2015.
 */
public class Word {
    private boolean immediate;
    private String name;
    private List<MemoryEntry> compiledCode = new ArrayList<>();

    public Word(String name, List<MemoryEntry> code, Boolean immediate) {
        this.name = name.toLowerCase();
        this.compiledCode = code;
        this.immediate = immediate;
    }

    public Word(String name, List<MemoryEntry> code) {
        this(name, code, false);
    }

    public String getName() {
        return name;
    }

    public List<MemoryEntry> getCode() {
        return compiledCode;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void makeImmediate() {
        immediate = true;
    }
}
