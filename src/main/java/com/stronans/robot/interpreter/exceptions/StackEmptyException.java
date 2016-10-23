package com.stronans.robot.interpreter.exceptions;

/**
 *
 * Created by S.King on 07/02/2015.
 */
public class StackEmptyException extends ArrayIndexOutOfBoundsException {

    private static final long serialVersionUID = 01L;

    public StackEmptyException() {
        super("Stack Empty");
    }

    public StackEmptyException(String message) {
        super(message);
    }

}
