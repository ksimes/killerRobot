package com.stronans.robot.engine.exceptions;

import java.io.Serial;

/**
 * Created by S.King on 08/02/2015.
 */
public class UnrecognisedTokenException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnrecognisedTokenException(String message) {
        super(message + " ?");
    }

}
