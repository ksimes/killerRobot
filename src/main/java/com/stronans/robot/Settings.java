package com.stronans.robot;

import com.stronans.robot.core.Dictionary;
import com.stronans.robot.core.StringLibrary;

/**
 * Contains the core setting for use across the entire application.
 * Created by S.King on 07/02/2015.
 */
public class Settings {
    private Dictionary dictionary = new Dictionary();
    private StringLibrary stringLibrary = new StringLibrary();
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Dictionary getDictionary() { return dictionary; }

    public StringLibrary getStringLibrary() { return stringLibrary; }
}
