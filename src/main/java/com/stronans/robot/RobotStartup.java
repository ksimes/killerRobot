package com.stronans.robot;

import com.stronans.motozero.motors.MotorController;
import com.stronans.robot.core.Common;
import com.stronans.robot.fileprocessing.ProcessFile;
import com.stronans.sensors.Sensors;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Startup class for the Giant Killer Robot controller program.
 * <p/>
 * Created by S.King on 07/02/2015.
 */
@Slf4j
public class RobotStartup {
    private static boolean TESTING = false;

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
            System.getProperties().load(RobotStartup.class.getClassLoader().getResourceAsStream("log4j.properties"));
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

        ProcessGoals processGoals = new ProcessGoals(args);
        processGoals.processFileArguments();
    }

    private static final class ProcessGoals {
        private final Settings settings = new Settings();
//        private final Sensors sensors;
//        private final MotorController motorController;
        private final String[] programArgs;

        ProcessGoals(String[] args) {

            programArgs = args;
            // See if there are any arguments which will affect the setting of controllers in this initialiser.
            processInitalArguments(args);

            // Startup all the threads which will handle:
            //   sight (Ultrasonic detectors)
//            log.info("Starting Sensor processing");
//            sensors = new Sensors(TESTING);
//            Thread sensorsThread = new Thread(sensors, "Sensors");
//            sensorsThread.start();
//            //   movement (wheels and later legs)
//            log.info("Starting Motor Controller processing");
//            motorController = new MotorController(TESTING);
//            Thread motorControl = new Thread(motorController, "Motors");
//            motorControl.start();
            //   temperature and humidity checks
            //   wifi transmission and reception (updates of goals and reports to central data server)
            //   Camera
            //   accelerometer and magnetometer (position and direction)

            // Load up all the words which will make the FORTH system work.
            InputStream is = getClass().getResourceAsStream("/base-words.robo");
            ProcessFile baseWordsContent = new ProcessFile(is, settings);
            baseWordsContent.process();
        }

        void shutdown() {
//            sensors.shutdown();
//            motorController.shutdown();
        }

        void processInitalArguments(String[] args) {

            log.info("Program startup");

            for (String argument : args) {
                log.trace("program arguments : [" + argument + "]");

                if (argument.startsWith("-")) {
                    switch (argument) {
                        case "-v", "-verbose":
                            settings.setVerbose(true);
                            break;

                        case "-t", "-test":
                            TESTING = true;
                            break;

                        case "-f",  "-file":
                            // Do nothing. Handled in the next routine in chain.
                            break;

                        default:
                            String msg = "Unrecognised argument : [" + argument + "]";
                            log.error(msg);
                            Common.outputln(msg);
                            break;
                    }
                }
            }
        }


        void processFileArguments() {
            int i;

            for (i = 0; i < programArgs.length; i++) {
                String argument = programArgs[i];
                if (argument.startsWith("-f")) {
                    switch (argument) {
                        case "-f", "-file" -> {
                            if (i + 1 < programArgs.length) {
                                ProcessFile pf = new ProcessFile(programArgs[++i], settings);
                                pf.process();
                            } else {
                                String msg = "-file requires a filename";
                                log.error(msg);
                                Common.outputln(msg);
                            }
                        }
                    }
                }
            }

            Common.outputln();
            log.info("Program complete");
            Common.outputln("ok");
            shutdown();

            // block the current thread for registerA duration in Milliseconds
            try {
                TimeUnit.MILLISECONDS.sleep(5000);
            } catch (InterruptedException e) {
                log.warn("5000 Millisecond sleep interrupted: " + e.getMessage(), e);
            }

            log.info("Thread list");
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

            for(Thread t : threadArray) {
                log.info("Thread name : " + t.getName());
            }
        }
    }
}
