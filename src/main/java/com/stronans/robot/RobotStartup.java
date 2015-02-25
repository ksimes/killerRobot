package com.stronans.robot;

import com.stronans.robot.core.Dictionary;
import com.stronans.robot.core.StringLibrary;
import com.stronans.robot.fileprocessing.ProcessFile;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.util.Properties;

/**
 * Startup class for the Giant Kill Robot controller program.
 * <p/>
 * Created by S.King on 07/02/2015.
 */
public class RobotStartup {
    private static final Logger logger = Logger.getLogger(RobotStartup.class);

    /**
     * Handles the loading of the log4j configuration. properties file must be
     * on the classpath.
     *
     * @throws RuntimeException
     */
    private static void initLogging() throws RuntimeException {
        try {
            Properties properties = new Properties();
            properties.load(RobotStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
            PropertyConfigurator.configure(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load logging properties for System");
        }
    }

    public static void main(String[] args) {
        try {
            initLogging();
        } catch (RuntimeException ex) {
            System.out.println("Error setting up log4j logging");
            System.out.println("Application will continue but without any logging.");
        }

        ProcessGoals processGoals = new ProcessGoals();
        processGoals.processArguments(args);
    }

    static final class ProcessGoals {
        Settings settings = new Settings();

        ProcessGoals() {
            // Startup all of the threads which will handle:
            //   sight (Ultrasonic detectors)
            //   movement (wheels and later legs)
            //   temperature and humidity checks
            //   wifi transmission and reception (updates of goals and reports to central data server)
            //   Camera
            //   accelerometer and magnetometer (position and direction)
        }

        void processArguments(String[] args) {
            int i = 0, j;
            String arg;
            char flag;

            for (String argument : args) {
                logger.trace("program arguments : [" + argument + "]");
            }

            InputStream is = getClass().getResourceAsStream("/base-words.robo");
            ProcessFile baseWordsContent = new ProcessFile(is, settings);
            baseWordsContent.process();

            while (i < args.length && args[i].startsWith("-")) {
                arg = args[i++];

                // use this type of check for "wordy" arguments
                if (arg.equals("-v") || arg.equals("-verbose")) {
                    settings.setVerbose(true);
                }

                // use this type of check for arguments that require arguments
                else if (arg.equals("-f") || arg.equals("-file")) {
                    if (i < args.length) {
                        ProcessFile pf = new ProcessFile(args[i++], settings);
                        pf.process();
                    } else {
                        logger.error("-file requires a filename");
                        System.err.println("-file requires a filename");
                    }
                }

                // use this type of check for a series of flag arguments
                else {
                    for (j = 1; j < arg.length(); j++) {
                        flag = arg.charAt(j);
                        switch (flag) {
                            case 'x':
//                                if (vflag) System.out.println("Option x");
                                break;
                            case 'n':
//                                if (vflag) System.out.println("Option n");
                                break;
                            default:
                                logger.error("illegal option " + flag);
                                System.err.println("illegal option " + flag);
                                break;
                        }
                    }
                }
            }

            System.out.println();
            logger.info("Program complete");
            System.out.println("ok");
        }
    }
}
