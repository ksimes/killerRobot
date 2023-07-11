package com.stronans.robot.engine.exceptions;

import java.io.Serial;

/**
 * Created by S.King on 23/02/2015.
 */
public class QuitException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public QuitException(String message) {
        super(message + " ?");
    }

    public QuitException() {
        super();
    }

}
