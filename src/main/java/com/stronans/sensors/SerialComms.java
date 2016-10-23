package com.stronans.sensors;

import com.pi4j.io.serial.*;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Serial communications.
 * Created by S.King on 21/05/2016.
 * Updated by S.King on 23/10/2016 to include option to modify the port baudrate speed.
 */
public class SerialComms {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(SerialComms.class);

    private static final int DEFAULT_SPEED = 115200;
    private String comPort = Serial.DEFAULT_COM_PORT;
    private int speed;

    private final Serial serial;
    private String message;
    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<>(20);

    public SerialComms(String port, int speed) throws InterruptedException {

        this.speed = speed;
        comPort = port;
        // create an instance of the serial communications class
        serial = SerialFactory.createInstance();

        // create and register the serial data listener
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                message = event.getData();

                if (message.endsWith("\n")) {
                    messages.add(message.substring(0, message.indexOf('\n')));
                }
            }
        });
    }

    public SerialComms() throws InterruptedException {
        this(Serial.DEFAULT_COM_PORT, DEFAULT_SPEED);
    }


    public SerialComms(String port) throws InterruptedException {
        this(port, DEFAULT_SPEED);
    }


    public ArrayBlockingQueue<String> messages() {
        return messages;
    }

    public void write(String message) {
        serial.write(message);
    }

    public void startComms() {
        try {
            // open the default serial port provided on the GPIO header
            serial.open(comPort, speed);
        } catch (SerialPortException ex) {
            log.error(" ==>> SERIAL SETUP FAILED : " + ex.getMessage(), ex);
        }
    }

    public void endComms() {
        try {
            // open the default serial port provided on the GPIO header
            serial.close();
        } catch (SerialPortException ex) {
            log.error(" ==>> SERIAL SHUTDOWN FAILED : " + ex.getMessage(), ex);
        }
    }
}