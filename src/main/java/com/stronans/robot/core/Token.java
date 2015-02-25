package com.stronans.robot.core;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Processing of the current buffered input stream into tokens and identifying the content of those tokens.
 * Created by S.King on 23/02/2015.
 */
public class Token {
    private static final String OP_CODE_ID = "|";

    private Dictionary dictionary;
    private BufferedInputStream inputStream;
    private String tokenContent;

    public Token(BufferedInputStream inputStream, Dictionary dictionary) {
        this.dictionary = dictionary;
        this.inputStream = inputStream;
    }

    public void clear()
    {
        tokenContent = "";
    }

    /**
     * While we have whitespace then skip it then pickup all no-whitespace characters until a new whitespace
     * character appears, this then is a token.
     * @return - true if we have a valid token and false if we hit the end of the stream.
     * @throws java.io.IOException
     */
    public boolean getToken() throws IOException {
        boolean finished;
        int content;
        boolean skipWhitespace = true;

        clear();

        do {
            content = inputStream.read();

            finished = content == -1;

            if (!finished) {
                if (skipWhitespace) {
                    skipWhitespace = Character.isWhitespace(content);
                    if (!skipWhitespace)
                        tokenContent += (char) content;
                } else {
                    if (!Character.isWhitespace(content))
                        tokenContent += (char) content;
                    else
                        finished = true;
                }
            }
        }
        while (!finished);

        return isValidToken();
    }

    /**
     * Checks that the token is not empty (zero length) and that if whitespace is removed that it is not empty (zero length).
     * @return true if a valid token otherwise false.
     */
    private boolean isValidToken()
    {
        return !tokenContent.isEmpty() && !tokenContent.trim().isEmpty();
    }

    /**
     * Checks if this token is already part of the word dictionary.
     * @return true if a word in the dictionary otherwise false.
     */
    public boolean isWord()
    {
        return dictionary.lookup(tokenContent) != null;
    }

    /**
     * Looks up this token in the word dictionary and returns it's reference.
     * @return The Word if it is in the dictionary or null otherwise.
     */
    public Word lookupWord()
    {
        return dictionary.lookup(tokenContent);
    }

    /**
     * Checks if this token is a number.
     * @return true if a word is a long format number.
     */
    public boolean isNumber()
    {
        boolean result;

        try {
            Long num = Long.parseLong(tokenContent);
            result = true;

        } catch (NumberFormatException e) {
            result = false;
        }

        return result;
    }

    /**
     * Converts this token to a number and returns its value.
     * @return The long number found.
     */
    public long asNumber()
    {
        long result;

        try {
            result = Long.parseLong(tokenContent);

        } catch (NumberFormatException e) {
            result = 0;
        }

        return result;
    }

    /**
     * Returns the token found as a text item
     * @return String of the text found
     */
    public String asText()
    {
        return tokenContent;
    }

    public boolean isOpCode() {
        boolean result = false;

        if (tokenContent.startsWith(OP_CODE_ID)) {
            result = OpCode.fromString(tokenContent.substring(1)) != null;
        }

        return result;
    }

    /**
     * Checks if this token is a number.
     * @return true if a word is a long format number.
     */
    public OpCode asOpCode() {
        OpCode opCode = null;

        if (tokenContent.startsWith(OP_CODE_ID)) {
            String code = tokenContent.substring(1);

            opCode = OpCode.fromString(code);
        }

        return opCode;
    }
}
