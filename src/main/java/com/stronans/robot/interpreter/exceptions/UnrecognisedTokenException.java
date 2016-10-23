package com.stronans.robot.interpreter.exceptions;

/**
 * Created by S.King on 08/02/2015.
 */
public class UnrecognisedTokenException extends Exception {

    private static final long serialVersionUID = 01L;

    public UnrecognisedTokenException(String message) {
        super(message + " ?");
    }

}
