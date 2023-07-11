package com.stronans.robot.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stronans.messagebus.MessageBus;
import com.stronans.motozero.messages.MotorMessage;
import com.stronans.motozero.messages.MotorMessages;
import com.stronans.robot.core.OpCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<OpCode, MotorMessages> mapping = new HashMap<>();

    public OutputOpcodes() {
        mapping.put(OpCode.Forwards, MotorMessages.Forwards);
        mapping.put(OpCode.Stop, MotorMessages.Stop);
        mapping.put(OpCode.Backwards, MotorMessages.Backwards);
        mapping.put(OpCode.Left, MotorMessages.Left);
        mapping.put(OpCode.Right, MotorMessages.Right);
        mapping.put(OpCode.HardLeft, MotorMessages.HardLeft);
        mapping.put(OpCode.HardRight, MotorMessages.HardRight);
        mapping.put(OpCode.Shutdown, MotorMessages.Shutdown);
        mapping.put(OpCode.Pause, MotorMessages.Pause);
    }

    void execute(OpCode opcode, Long register) {
        sendMessage(mapping.get(opcode), register);
    }

    private void sendMessage(MotorMessages message, Long action) {
        MotorMessage payload = new MotorMessage(message.name(), action.intValue());

        try {
            String data = mapper.writeValueAsString(payload);
            log.debug("outgoing msg : " + data);
            messageBus.addMessage(DRIVER, data);
        } catch (JsonProcessingException jpe) {
            log.error(" ==>> FAILED TO SERIALISE JSON MESSAGE: " + jpe.getMessage(), jpe);
        }
    }
}
