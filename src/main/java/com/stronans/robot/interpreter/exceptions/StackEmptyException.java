package com.stronans.robot.interpreter.exceptions;

/**
 *
 * Created by S.King on 07/02/2015.
 */
public class StackEmptyException extends ArrayIndexOutOfBoundsException {

    public StackEmptyException() {
        super("Stack Empty");
    }

    public StackEmptyException(String message) {
        super(message);
    }

}
