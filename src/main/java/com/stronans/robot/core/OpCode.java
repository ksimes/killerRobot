package com.stronans.robot.core;

/**
 *
 * Created by S.King on 07/02/2015.
 */
public enum OpCode {
    toInterpretMode,
    toCompileMode,
    processComment,
    processComment2,
    processString,
    makeImmediate,
    ifTest,             // (n -- )
    thenJump,
    elseJump,
    dumpDictionary,
    pushA,              // ( -- n)
    popA,               // (n -- )
    popRA,              // (r -- )
    pushRA,             // ( -- n)
    incA,
    decA,
    peekRA,             // (n -- r)
    pushB,              // ( -- n)
    popB,               // (n -- )
    pushRB,             // (n -- r)
    incB,
    decB,
    jumpEqAB,
    jumpANEq0,
    addAB,
    subAB,
    mulAB,
    divAB,
    equalAB,
    lessAB,
    greaterAB,
    printB,
    printA,
    emitA,
    emitB,
    addressR,
    pushAddressR,
    doStart,
    loop,
    plusLoop,
    begin,
    again,
    quit,
    load,
    step,               // (n n n -- ) Steps, side, precision
    distance            // ( -- n)
    ;

    public static OpCode fromString(String name) {
        return Common.getEnumFromString(OpCode.class, name);
    }
}
