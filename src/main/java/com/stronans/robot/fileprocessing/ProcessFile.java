package com.stronans.robot.fileprocessing;

import com.stronans.robot.Settings;
import com.stronans.robot.core.Dictionary;
import com.stronans.robot.core.StringLibrary;
import com.stronans.robot.interpreter.Interpreter;

import java.io.*;

/**
 * loads and processes a 'Robo' file contain FORTH like commands.
 * Created by S.King on 07/02/2015.
 */
public class ProcessFile {
    private String fileName = null;
    private InputStream inputStream = null;
    private Settings settings = null;

    private ProcessFile(Settings settings) {
        this.settings = settings;
    }

    public ProcessFile(String fileName, Settings settings) {
        this(settings);
        this.fileName = fileName;
    }

    public ProcessFile(InputStream inputStream, Settings settings) {
        this(settings);
        this.inputStream = inputStream;
    }

    public void process() {
        BufferedInputStream characterStream = null;

        try {
            if (fileName != null) {
                File file = new File(fileName);
                characterStream = new BufferedInputStream(new FileInputStream(file));
            } else {
                characterStream = new BufferedInputStream(this.inputStream);
            }

            if (settings.isVerbose()) {
                System.out.println("Total file size to read (in bytes) : " + characterStream.available());
            }

            Interpreter interpreter = new Interpreter(settings, characterStream);
            interpreter.processStream();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (characterStream != null)
                    characterStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
