package com.stronans.robot.interpreter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stronans.messagebus.MessageBus;
import com.stronans.motozero.messages.MotorMessage;
import com.stronans.motozero.messages.MotorMessages;
import com.stronans.robot.core.OpCode;
import org.apache.log4j.Logger;

import static com.stronans.motozero.motors.MotorController.DRIVER;

/**
 * Handles the special OpCodes which control the Robot motors.
 *
 * Created by S.King on 08/10/2016.
 */
class SpecialOpcodes {
    private static final Logger logger = Logger.getLogger(SpecialOpcodes.class);
    private MessageBus messageBus = MessageBus.getInstance();
    private ObjectMapper mapper = new ObjectMapper();


    void execute(OpCode opcode, Long register) {
        switch (opcode) {
            case Forwards:
                sendMessage(MotorMessages.Forwards, register);
                break;

            case Stop:
                sendMessage(MotorMessages.Stop, 0L);
                break;

            case Backwards:
                sendMessage(MotorMessages.Backwards, register);
                break;

            case Left:
                sendMessage(MotorMessages.Left, register);
                break;

            case Right:
                sendMessage(MotorMessages.Right, register);
                break;

            case HardLeft:
                sendMessage(MotorMessages.HardLeft, register);
                break;

            case HardRight:
                sendMessage(MotorMessages.HardRight, register);
                break;

            case Shutdown:
                sendMessage(MotorMessages.Shutdown, register);
                break;

            case Pause:
                sendMessage(MotorMessages.Pause, register);
                break;
        }
    }

    private void sendMessage(MotorMessages message, Long speed) {
        MotorMessage payload = new MotorMessage(message.name(), speed.intValue());

        try {
            String data = mapper.writeValueAsString(payload);
            logger.debug("outgoing msg : " + data);
            messageBus.addMessage(DRIVER, data);
        } catch (JsonProcessingException jpe) {
            logger.error(" ==>> FAILED TO SERIALISE JSON MESSAGE: " + jpe.getMessage(), jpe);
        }
    }
}
