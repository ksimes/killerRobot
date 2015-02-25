package com.stronans.robot.core;

import com.stronans.robot.interpreter.exceptions.StackEmptyException;

import java.util.Stack;

/**
 *
 * Created by S.King on 23/02/2015.
 */
public class CoreStack extends Stack<Long> {
    @Override
    public synchronized Long pop() throws StackEmptyException {
        if(super.empty())
        {
            throw new StackEmptyException();
        }
        return super.pop();
    }
}
