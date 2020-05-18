package com.itis.javalab.jlmq.config;

import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocket
@EnableWebSocketMessageBroker
@Configuration
public class StompConfig implements WebSocketMessageBrokerConfigurer {
    private SubscriptionService subscriptionService;
    private MessageService messageService;

    public StompConfig(SubscriptionService subscriptionService, MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
    }

    @Autowired
    @Lazy
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/jlmq").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/producer", "/consumer");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new SubscriptionInterceptor(subscriptionService, messageService), new DisconnectAndUnsubscribeInterceptor(subscriptionService), new ACKInterceptor(messageService, simpMessagingTemplate));
    }

}
