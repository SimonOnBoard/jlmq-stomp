package com.itis.javalab.jlmq.config;

import com.itis.javalab.jlmq.services.interfaces.SubscriptionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class DisconnectAndUnsubscribeInterceptor implements ChannelInterceptor {
    private SubscriptionService subscriptionService;

    public DisconnectAndUnsubscribeInterceptor(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            subscriptionService.removeSession(accessor.getSessionId());
        }
        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())){
            subscriptionService.removeSubscription(accessor.getDestination(), accessor.getSessionId());
        }
        return message;
    }

}
