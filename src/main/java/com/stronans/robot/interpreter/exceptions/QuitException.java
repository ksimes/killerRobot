package com.stronans.robot.interpreter.exceptions;

/**
 * Created by S.King on 23/02/2015.
 */
public class QuitException extends Exception {

    public QuitException(String message) {
        super(message + " ?");
    }

    public QuitException() {
        super();
    }

}
