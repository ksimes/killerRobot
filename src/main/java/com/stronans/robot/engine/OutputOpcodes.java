package com.stronans.robot.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stronans.messagebus.MessageBus;
import com.stronans.motozero.messages.MotorMessage;
import com.stronans.robot.core.OpCode;
import lombok.extern.slf4j.Slf4j;

import static com.stronans.motozero.motors.MotorController.DRIVER;

/**
 * Handles the special OpCodes which control the Robots motors/legs.
 * Upgraded from Java 1.8 to 17, July 2023.
 * <p/>
 * Created by S.King on 08/10/2016 for motors (motozero control driving tracks).
 * Updated by S.King on 10/07/2023 generic driver for motors or legs.
 */
@Slf4j
class OutputOpcodes {
    private final MessageBus messageBus = MessageBus.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public OutputOpcodes() {
    }

    void execute(OpCode opcode, Long registerA, Long registerB) {
        sendMessage(opcode, registerA, registerB);
    }

    private void sendMessage(OpCode opcode, Long action, Long behaviour) {
        MotorMessage payload = new MotorMessage(opcode.name(), action.intValue());

        try {
            String data = mapper.writeValueAsString(payload);
            log.debug("outgoing msg : " + data);
            messageBus.addMessage(DRIVER, data);
        } catch (JsonProcessingException jpe) {
            log.error(" ==>> FAILED TO SERIALISE JSON MESSAGE: " + jpe.getMessage(), jpe);
        }
    }
}
