package com.itis.javalab.jlmq.controllers;


import com.itis.javalab.jlmq.dto.MessageDto;
import com.itis.javalab.jlmq.services.interfaces.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consumer/**")
public class ConsumerController{
    private MessageService messageService;
    private SimpMessagingTemplate messagingTemplate;
    public ConsumerController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/queue/{id}")
    public void getMessageForQueue(@DestinationVariable("id") String name){
        MessageDto messageDto = null;
        if((messageDto = messageService.findMessageFor(name)) != null){
            messagingTemplate.convertAndSend("/queue/" + messageDto.getQueue(),messageDto);
        }
    }
}
