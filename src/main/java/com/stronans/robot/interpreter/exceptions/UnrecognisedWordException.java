package com.stronans.robot.interpreter.exceptions;

/**
 * Created by S.King on 08/02/2015.
 */
public class UnrecognisedWordException extends Exception {

    public UnrecognisedWordException(String message) {
        super(message + " ?");
    }

}
