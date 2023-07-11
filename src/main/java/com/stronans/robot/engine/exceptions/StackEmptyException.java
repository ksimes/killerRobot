package com.stronans.robot.engine.exceptions;

import java.io.Serial;

/**
 *
 * Created by S.King on 07/02/2015.
 */
public class StackEmptyException extends ArrayIndexOutOfBoundsException {

    @Serial
    private static final long serialVersionUID = 1L;

    public StackEmptyException() {
        super("Stack Empty");
    }

    public StackEmptyException(String message) {
        super(message);
    }

}
