package com.stronans.robot;

import com.stronans.motozero.motors.MotorController;
import com.stronans.robot.core.Common;
import com.stronans.robot.fileprocessing.ProcessFile;
import com.stronans.sensors.Sensors;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.util.Properties;

/**
 * Startup class for the Giant Killer Robot controller program.
 * <p/>
 * Created by S.King on 07/02/2015.
 */
public class RobotStartup {
    private static final Logger logger = Logger.getLogger(RobotStartup.class);

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

    private static final class ProcessGoals {
        Settings settings = new Settings();
        Sensors sensors;
        MotorController motorController;

        ProcessGoals() {
            // Startup all of the threads which will handle:
            //   sight (Ultrasonic detectors)
            logger.info("Starting Sensor processing");
            sensors = new Sensors(TESTING);
            Thread sensorsThread = new Thread(sensors);
            sensorsThread.start();
            //   movement (wheels and later legs)
            logger.info("Starting Motor Controller processing");
            motorController = new MotorController(TESTING);
            Thread motorControl = new Thread(motorController);
            motorControl.start();
            //   temperature and humidity checks
            //   wifi transmission and reception (updates of goals and reports to central data server)
            //   Camera
            //   accelerometer and magnetometer (position and direction)
        }

        void shutdown()
        {
            sensors.shutdown();
//            motorController.shutdown();
        }

        void processArguments(String[] args) {
            int i = 0, j;
            String arg;
            char flag;

            logger.info("Program startup");

            for (String argument : args) {
                logger.trace("program arguments : [" + argument + "]");
            }

            InputStream is = getClass().getResourceAsStream("/base-words.robo");
            ProcessFile baseWordsContent = new ProcessFile(is, settings);
            baseWordsContent.process();

            while (i < args.length && args[i].startsWith("-")) {
                arg = args[i++];

                switch (arg) {
                    case "-v":
                    case "-verbose":
                        settings.setVerbose(true);
                        break;

                    case "-f":
                    case "-file":
                        if (i < args.length) {
                            ProcessFile pf = new ProcessFile(args[i++], settings);
                            pf.process();
                        } else {
                            logger.error("-file requires a filename");
                            Common.outputln("-file requires a filename");
                        }
                        break;

//                    case "-t":
//                    case "-test":
//                        TESTING = true;
//                        break;
//
                    default:
                        for (j = 1; j < arg.length(); j++) {
                            flag = arg.charAt(j);
                            switch (flag) {
//                                case 'x':
//                                if (vflag) System.out.println("Option x");
//                                    break;
//                                case 'n':
//                                if (vflag) System.out.println("Option n");
//                                    break;
                                default:
                                    logger.error("illegal option " + flag);
                                    Common.outputln("illegal option " + flag);
                                    break;
                            }
                        }
                }
            }

            Common.outputln();
            logger.info("Program complete");
            Common.outputln("ok");
            shutdown();
        }
    }
}
