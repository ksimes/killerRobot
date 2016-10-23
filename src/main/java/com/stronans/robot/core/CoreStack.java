package com.stronans.robot.core;

import com.stronans.robot.interpreter.exceptions.StackEmptyException;

import java.util.Stack;

/**
 *
 * Created by S.King on 23/02/2015.
 */
public final class CoreStack extends Stack<Long> {
    private static final long serialVersionUID = 01L;

    @Override
    public synchronized Long pop() throws StackEmptyException {
        if(super.empty())
        {
            throw new StackEmptyException();
        }
        return super.pop();
    }
}
