package com.itis.javalab.jlmq.config;

import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SubscriptionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;


public class SubscriptionInterceptor implements ChannelInterceptor {
    private SubscriptionService subscriptionService;
    private MessageService messageService;

    public SubscriptionInterceptor(SubscriptionService subscriptionService, MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            destination = getRealDestination(destination);
            subscriptionService.subscribe(destination,accessor.getSessionId());
        }
        return message;
    }

    private String getRealDestination(String destination) {
        String[] strings = destination.split("\\/");
        if (strings.length != 3) throw new IllegalStateException("Invalid path");
        return strings[2];
    }


}
