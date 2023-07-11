package com.stronans.robot.core;

import com.stronans.robot.engine.exceptions.StackEmptyException;

import java.io.Serial;
import java.util.Stack;

/**
 *
 * Created by S.King on 23/02/2015.
 */
public final class CoreStack extends Stack<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public synchronized Long pop() throws StackEmptyException {
        if(super.empty())
        {
            throw new StackEmptyException();
        }
        return super.pop();
    }
}
