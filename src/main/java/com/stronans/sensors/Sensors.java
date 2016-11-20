package com.stronans.sensors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

/**
 * Processes and stores any sensor data picked up from serial comms.
 * Created by S.King on 22/10/2016.
 */
public class Sensors implements Runnable {
    /**
     * The <code>Logger</code> to be used.
     */
    private final static Logger log = Logger.getLogger(Sensors.class);
    private final static String SERIAL_PORT = "/dev/ttyUSB0";
    private boolean testing = false;
    private static SerialComms comms;
    private ObjectMapper mapper = new ObjectMapper();
    private static SensorMessage lastReading = new SensorMessage(new Distance(-1L, -1L, -1L));
    private static boolean finished = false;

    public Sensors() {
    }

    public Sensors(boolean testing) {
        this.testing = testing;
        if (!testing) {
            try {
                comms = new SerialComms(SERIAL_PORT);
                comms.startComms();

            } catch (InterruptedException e) {
                log.error(" ==>> PROBLEMS WITH SERIAL COMMUNICATIONS: " + e.getMessage(), e);
            }
        }
    }

    private void processMessage(String rawMessage) {
        if (!testing) {
            log.info("incomming msg : [" + rawMessage + "]");

            try {
                synchronized (Sensors.class) {
                    lastReading = mapper.readValue(rawMessage, SensorMessage.class);

                    log.info("Last Reading : " + lastReading);
                }

            } catch (JsonParseException jpe) {
                // Throw away the corrupt message and go on to the next one.
                log.error(" ==>> FAILED TO DESERIALISE JSON MESSAGE: " + jpe.getMessage());
            } catch (Exception e) {
                // Throw away the corrupt message and go on to the next one.
                log.error(" ==>> EXCEPTION DESERIALISING JSON MESSAGE: " + e.getMessage());
            }
        } else {
            Distance distance = new Distance(20L, 20L, 20L);
            lastReading = new SensorMessage(distance);
        }
    }

    @Override
    public void run() {
        try {

            while (!finished) {
                String message;

                if (!testing) {
                    message = comms.messages().take();
                } else {
                    message = "";
                }

                processMessage(message);
            }

        } catch (InterruptedException e) {
            log.error(" ==>> PROBLEMS WITH SERIAL COMMUNICATIONS: " + e.getMessage(), e);
        }

        log.info("Sensor processing shutdown");
    }

    public void shutdown() {
        finished = true;
        if (!testing) {
            comms.endComms();
        }
    }

    static public long getSensorData(long sensorID) {
        long result = 0;
        int sensorToRead = (int) sensorID;

        synchronized (Sensors.class) {
            switch (sensorToRead) {
                case 1:     // Left side mounted ultrasonic sensor
                    result = lastReading.getDistance().getLeft();
                    break;

                case 2:     // Center mounted ultrasonic sensor
                    result = lastReading.getDistance().getCentre();
                    break;

                case 3:     // Right side mounted ultrasonic sensor
                    result = lastReading.getDistance().getRight();
                    break;
            }
        }

        return result;
    }
}
