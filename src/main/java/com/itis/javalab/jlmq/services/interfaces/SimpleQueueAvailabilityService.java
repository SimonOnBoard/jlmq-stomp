package com.itis.javalab.jlmq.services.interfaces;

public interface SimpleQueueAvailabilityService {
    boolean checkSessionAvailable(String queue);

    void makeFree(String queue);

    void makeBusy(String queue);

    void addQueue(String queue);

    Boolean exists(String queue);

    void remove(String queue);
}
