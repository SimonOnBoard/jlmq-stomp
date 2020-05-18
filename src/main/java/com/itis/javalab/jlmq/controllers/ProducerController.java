package com.itis.javalab.jlmq.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.itis.javalab.jlmq.dto.MessageDto;
import com.itis.javalab.jlmq.dto.ProducerUniformMessage;
import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SimpleQueueAvailabilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/producer/**")
@Slf4j
public class ProducerController {
    private MessageService messageService;
    private SimpMessagingTemplate messagingTemplate;
    private SimpleQueueAvailabilityService simpleQueueAvailabilityService;

    public ProducerController(MessageService messageService, SimpMessagingTemplate simpMessagingTemplate, SimpleQueueAvailabilityService simpleQueueAvailabilityService) {
        this.messageService = messageService;
        this.messagingTemplate = simpMessagingTemplate;
        this.simpleQueueAvailabilityService = simpleQueueAvailabilityService;
    }

    @MessageMapping("/task")
    public void addNewTask(Message<ProducerUniformMessage<?>> message) throws JsonProcessingException {
        log.info("Received a message from producer: " + message.getPayload().toString());
        ProducerUniformMessage<?> simpleTextMessageProducerUniformMessage = message.getPayload();
        MessageDto messageDto = messageService.save(simpleTextMessageProducerUniformMessage);
        if (simpleQueueAvailabilityService.checkSessionAvailable(messageDto.getQueue())) {
            simpleQueueAvailabilityService.makeBusy(messageDto.getQueue());
            messagingTemplate.convertAndSend("/queue/" + simpleTextMessageProducerUniformMessage.getQueue(),
                    messageDto);
        }
    }

    @MessageMapping("/startProducer")
    public void startNewProducer(Message<ProducerUniformMessage<?>> message) {
        ProducerUniformMessage<?> simpleTextMessageProducerUniformMessage = message.getPayload();
        System.out.println(simpleTextMessageProducerUniformMessage);
    }
}
