package com.itis.javalab.jlmq.services.interfaces;

import com.itis.javalab.jlmq.models.Queue;
import org.springframework.web.socket.WebSocketSession;

public interface SubscriptionService {
    void subscribe(String queue, String sessionId);

    void removeSession(String sessionId);

    void removeSubscription(String destination, String sessionId);
}
