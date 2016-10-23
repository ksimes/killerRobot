package com.stronans.robot.core;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Processing of the current buffered input stream into tokens and identifying the content of those tokens.
 * Created by S.King on 23/02/2015.
 */
public final class Token {
    private static final String OP_CODE_ID = "|";

    private final Dictionary dictionary;
    private final BufferedInputStream inputStream;
    private String tokenContent;

    public Token(BufferedInputStream inputStream, Dictionary dictionary) {
        this.dictionary = dictionary;
        this.inputStream = inputStream;
    }

    public void clear() {
        tokenContent = "";
    }

    /**
     * While we have whitespace then skip it then pickup all no-whitespace characters until a new whitespace
     * character appears, this then is a token.
     *
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
     *
     * @return true if a valid token otherwise false.
     */
    private boolean isValidToken() {
        return !tokenContent.isEmpty() && !tokenContent.trim().isEmpty();
    }

    /**
     * Looks up this token in the word dictionary and returns it's reference.
     *
     * @return Optional containing the word if it is in the dictionary otherwise returns absent.
     */
    public Optional<Word> asWord() {
        Word word = dictionary.lookup(tokenContent);

        if (word == null)
            return Optional.empty();
        else
            return Optional.of(word);
    }

    /**
     * Converts this token to a number and returns its value.
     *
     * @return The long number found.
     */
    public Optional<Long> asNumber() {
        long result;

        try {
            result = Long.parseLong(tokenContent);

        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

    /**
     * Returns the token found as a text item
     *
     * @return String of the text found
     */
    public String asText() {
        return tokenContent;
    }

    /**
     * Checks if this token is a number.
     *
     * @return Optional containing the word as a long format number or absent.
     */
    public Optional<OpCode> asOpCode() {
        OpCode opCode = null;

        if (tokenContent.startsWith(OP_CODE_ID)) {
            String code = tokenContent.substring(1);

            opCode = OpCode.fromString(code);
        }

        if (opCode == null)
            return Optional.empty();
        else
            return Optional.of(opCode);
    }
}
