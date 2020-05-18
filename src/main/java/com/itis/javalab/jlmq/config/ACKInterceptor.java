package com.itis.javalab.jlmq.config;


import com.itis.javalab.jlmq.dto.MessageDto;
import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.MessageDigest;
import java.util.Objects;

public class ACKInterceptor implements ChannelInterceptor {
    private MessageService messageService;

    public ACKInterceptor(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        int caseNumber;
        if (StompCommand.ACK.equals(accessor.getCommand())) {
            caseNumber = Integer.parseInt(accessor.getFirstNativeHeader("status"));
            switch (caseNumber) {
                case 1:
                    messageService.acknowledged(accessor.getFirstNativeHeader("messageId"));
                    break;
                case 2:
                    String queue = messageService.completed(accessor.getFirstNativeHeader("messageId"));
                    MessageDto messageDto = null;
                    if (queue != null && ((messageDto = messageService.findMessageFor(queue)) != null)) {
                        messagingTemplate.convertAndSend("/queue/" + messageDto.getQueue(), messageDto);
                    }
                    break;
            }
        }
        return message;
    }

}
