package com.itis.javalab.jlmq.services.implementations;

import com.itis.javalab.jlmq.services.interfaces.SimpleQueueAvailabilityService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SimpleQueueAvailabilityServiceImpl implements SimpleQueueAvailabilityService {
    private final Map<String, Boolean> availableQueues = new HashMap<>();

    @Override
    public boolean checkSessionAvailable(String queue) {
        Boolean result = availableQueues.get(queue);
        if (result != null) {
            return result;
        }
        return false;
    }

    @Override
    public void makeFree(String queue) {
        addQueue(queue);
    }

    @Override
    public void makeBusy(String queue) {
        availableQueues.put(queue, false);
    }

    @Override
    public void addQueue(String queue) {
        log.info("Added queue: " + queue);
        availableQueues.put(queue, true);
    }

    @Override
    public Boolean exists(String queue) {
        return availableQueues.get(queue);
    }

    @Override
    @Synchronized
    public void remove(String queue) {
        availableQueues.remove(queue);
    }
}
