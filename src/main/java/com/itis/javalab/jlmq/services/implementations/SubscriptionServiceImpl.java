package com.itis.javalab.jlmq.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itis.javalab.jlmq.models.Queue;
import com.itis.javalab.jlmq.repositories.QueueRepository;
import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SimpleQueueAvailabilityService;
import com.itis.javalab.jlmq.services.interfaces.SubscriptionService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Map<String, List<String>> sessionMap = new HashMap<>();
    private QueueRepository queueRepository;
    private SimpleQueueAvailabilityService queueAvailabilityService;
    private ObjectMapper objectMapper;

    public SubscriptionServiceImpl(QueueRepository queueRepository, SimpleQueueAvailabilityService queueAvailabilityService, ObjectMapper objectMapper) {
        this.queueRepository = queueRepository;
        this.queueAvailabilityService = queueAvailabilityService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Synchronized
    public void subscribe(String destination, String sessionId) {
        Optional<Queue> queueCandidate = queueRepository.findByName(destination);
        Queue q = queueCandidate.orElseThrow(() -> new IllegalStateException(getExceptionMessage("Not a queue", destination, 1)));
        if (queueAvailabilityService.exists(q.getName()) != null)
            throw new IllegalStateException(getExceptionMessage("Already have consumer", q.getName(), 1));
        queueAvailabilityService.addQueue(q.getName());
        if (sessionMap.get(sessionId) != null) {
            sessionMap.get(sessionId).add(q.getName());
        } else {
            sessionMap.put(sessionId, new ArrayList<String>());
            sessionMap.get(sessionId).add(q.getName());
        }
    }

    private String getExceptionMessage(String exception, String queue, int type) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("exception", exception);
        parameters.put("queue", queue);
        parameters.put("type", "" + type);
        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void removeSession(String sessionId) {
        for (String name : sessionMap.get(sessionId)) {
            queueAvailabilityService.remove(name);
        }
        sessionMap.get(sessionId).clear();
    }

    @Override
    public void removeSubscription(String destination, String sessionId) {
        log.info("Unsubscribing: " + destination + " session: " + sessionId);
        String[] strings = destination.split("\\/");
        if (strings.length != 3) throw new IllegalStateException(getExceptionMessage("Invalid address", strings[3], 2));
        Optional<Queue> queueCandidate = queueRepository.findByName(strings[2]);
        Queue q = queueCandidate.orElseThrow(() -> new IllegalStateException(getExceptionMessage("Not a queue", destination, 1)));
        sessionMap.get(sessionId).remove(q.getName());
        queueAvailabilityService.remove(q.getName());
    }
}
