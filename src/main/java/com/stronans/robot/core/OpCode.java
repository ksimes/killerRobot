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
    delay,              // (n -- )      Delays program for defined number of Milliseconds
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
    incB,               // (n -- n)
    decB,               // (n -- n)
    jumpEqAB,
    jumpANEq0,
    addAB,
    subAB,
    mulAB,
    divAB,
    equalAB,            // (n n -- )
    lessAB,             // (n n -- )
    greaterAB,          // (n n -- )
    andAB,              // (n n -- )
    orAB,               // (n n -- )
    printB,
    printA,
    emitA,              // (n -- )
    emitB,              // (n -- )
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
    distance,           // ( n n)

    Forwards,
    Stop,
    Backwards,
    Left,
    Right,
    HardLeft,
    HardRight,
    Shutdown,
    Pause,

    buildVariable,
    quitOut,
    storeVariable,
    fetchVariable,
    buildConstant,

    picture
    ;

    public static OpCode fromString(String name) {
        return Common.getEnumFromString(OpCode.class, name);
    }
}
