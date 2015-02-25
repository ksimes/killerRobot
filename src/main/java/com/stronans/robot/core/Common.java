package com.stronans.robot.core;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 *
 * Created by S.King on 08/02/2015.
 */
public class Common {

    public static String processString(BufferedInputStream fis, char delimiter) throws IOException {
        String buffer = "";
        boolean finished;
        int content;

        do {
            content = fis.read();

            finished = content == -1;

            if (!finished) {
                if ((char) content != delimiter)
                    buffer += (char) content;
                else
                    finished = true;
            }
        }
        while (!finished);

        return buffer;
    }

    /**
     * A common method for all enums since they can't have another base class
     *
     * @param <T>    Enum type
     * @param c      enum type. All enums must be all caps.
     * @param string case insensitive
     * @return corresponding enum, or null
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        T result = null;
        if (c != null && string != null) {
            try {
                result = Enum.valueOf(c, string);
            } catch (IllegalArgumentException ex) {
                result = null;
            }
        }
        return result;
    }

    public static void emitString(String text) {
        System.out.print(text);
    }

    public static void emitNumber(long number) {
        System.out.print(number + " ");
    }

    public static void emitChar(int number) {
        System.out.print((char) number);
    }

}
