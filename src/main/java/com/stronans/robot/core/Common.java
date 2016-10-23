package com.stronans.robot.core;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Routines used all over the projects.
 * <p>
 * Created by S.King on 08/02/2015.
 */
public class Common {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(Common.class);


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
    static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
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
        output(text);
    }

    public static void emitNumber(Long number) {
        output(number + " ");
    }

    public static void emitChar(int number) {
        output((char) number);
    }

    public static void outputln(String text) {
        log.debug(text);
        System.out.println(text);
    }

    public static void outputln() {
        System.out.println();
    }

    public static void output(String text) {
        log.debug(text);
        System.out.print(text);
    }

    public static void output(char character) {
        log.debug(character);
        System.out.print(character);
    }
}
