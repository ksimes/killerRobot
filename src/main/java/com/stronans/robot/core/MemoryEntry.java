package com.stronans.robot.core;

/**
 *
 * Created by S.King on 07/02/2015.
 */
public final class MemoryEntry {
    MemoryType type;
    OpCode operation;
    long number;
    Word word;
    String stringKey;
    int address;

    public MemoryEntry(OpCode operation) {
        type = MemoryType.OpCode;
        this.operation = operation;
    }

    public MemoryEntry(long number) {
        type = MemoryType.Number;
        this.number = number;
    }

    public MemoryEntry(int address) {
        type = MemoryType.Address;
        this.address = address;
    }

    public MemoryEntry(Word word) {
        type = MemoryType.Word;
        this.word = word;
    }

    public MemoryEntry(String stringKey) {
        type = MemoryType.StringPointer;
        this.stringKey = stringKey;
    }

    public MemoryType getType() {
        return type;
    }

    public OpCode getOperation() {
        return operation;
    }

    public Word getWord() {
        return word;
    }

    public long getNumber() {
        return number;
    }

    public String getStringKey() {
        return stringKey;
    }

    public int getAddress() {
        return address;
    }
}
