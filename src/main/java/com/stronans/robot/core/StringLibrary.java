package com.stronans.robot.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all strings and returns them during interpretation/compiling.
 * Created by S.King on 08/02/2015.
 */
public class StringLibrary {
    // Allows for 2 billion keys
    private int indexKey = 0;
    private final Map<String, String>Strings = new HashMap<>(200);

    public String add(String data)
    {
        String key = String.valueOf(++indexKey);
        Strings.put(key, data);
        return key;
    }

    public String get(String key)
    {
        return Strings.get(key);
    }
}
