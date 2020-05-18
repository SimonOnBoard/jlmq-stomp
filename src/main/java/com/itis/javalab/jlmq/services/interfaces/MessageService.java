package com.itis.javalab.jlmq.services.interfaces;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.itis.javalab.jlmq.dto.MessageDto;
import com.itis.javalab.jlmq.dto.ProducerUniformMessage;
import com.itis.javalab.jlmq.models.Status;

import java.util.Optional;

public interface MessageService {

    MessageDto findMessageFor(String queue);

    MessageDto save(ProducerUniformMessage<?> jsonNode) throws JsonProcessingException;

    Optional<String> updateStatus(Status acknowledged, String messageId);

    void acknowledged(String messageId);

    String completed(String messageId);
}
