package com.stronans.robot.fileprocessing;

import com.stronans.robot.Settings;
import com.stronans.robot.core.Common;
import com.stronans.robot.engine.Interpreter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;

/**
 * loads and processes a 'Robo' file contain FORTH like commands.
 * Created by S.King on 07/02/2015.
 */
@Slf4j
public class ProcessFile {
    private String fileName = null;
    private InputStream inputStream = null;
    private final Settings settings;

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
                characterStream = new BufferedInputStream(Files.newInputStream(file.toPath()));

                if (settings.isVerbose()) {
                    Common.outputln("File to read : " + fileName);
                    Common.outputln("Total file size (in bytes) : " + characterStream.available());
                }
            } else {
                characterStream = new BufferedInputStream(this.inputStream);
                if (settings.isVerbose()) {
                    Common.outputln("Total stream size (in bytes) : " + characterStream.available());
                }
            }

            Interpreter interpreter = new Interpreter(settings, characterStream);
            interpreter.processStream();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception Reading file ", e);
        } finally {
            try {
                if (characterStream != null)
                    characterStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                log.error("Exception Closing charcater stream ", ex);
            }
        }
    }
}
