package com.itis.javalab.jlmq.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itis.javalab.jlmq.dto.MessageDto;
import com.itis.javalab.jlmq.dto.ProducerUniformMessage;
import com.itis.javalab.jlmq.models.Message;
import com.itis.javalab.jlmq.models.Queue;
import com.itis.javalab.jlmq.models.Status;
import com.itis.javalab.jlmq.repositories.MessageRepository;
import com.itis.javalab.jlmq.repositories.QueueRepository;
import com.itis.javalab.jlmq.services.interfaces.MessageService;
import com.itis.javalab.jlmq.services.interfaces.SimpleQueueAvailabilityService;
import com.itis.javalab.jlmq.services.interfaces.TokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MessageServiceImpl implements MessageService {
    private MessageRepository messageRepository;
    private QueueRepository queueRepository;
    private TokenGenerator tokenGenerator;
    private SimpleQueueAvailabilityService availabilityService;
    private ObjectMapper objectMapper;

    public MessageServiceImpl(MessageRepository messageRepository, QueueRepository queueRepository, TokenGenerator tokenGenerator, SimpleQueueAvailabilityService availabilityService, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.queueRepository = queueRepository;
        this.tokenGenerator = tokenGenerator;
        this.availabilityService = availabilityService;
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageDto save(ProducerUniformMessage<?> data) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(data.getPayload());
        Optional<Queue> queueCandidate = queueRepository.findByName(data.getQueue());
        Queue q = queueCandidate.orElse(null);
        Assert.notNull(q, "can't be null");
        Message message = Message.builder()
                .id(tokenGenerator.getToken())
                .queue(q)
                .payload(payload)
                .start(LocalDateTime.now())
                .status(Status.ASSIGNED)
                .build();
        messageRepository.save(message);
        return MessageDto.from(message.getQueue().getName(), message.getId(), "do", objectMapper.readTree(message.getPayload()));
    }

    @Override
    public Optional<String> updateStatus(Status status, String messageId) {
        Optional<Message> messageCandidate = messageRepository.findById(messageId);
        String name = null;
        if (messageCandidate.isPresent()) {
            Message message = messageCandidate.get();
            message.setStatus(status);
            messageRepository.save(message);
            name = message.getQueue().getName();
        }
        return Optional.ofNullable(name);
    }

    @Override
    public void acknowledged(String messageId) {
        this.updateStatus(Status.ACKNOWLEDGED, messageId);
    }

    @Override
    public String completed(String messageId) {
        AtomicReference<String> toReturn = new AtomicReference<>();
        this.updateStatus(Status.COMPLETED, messageId).ifPresent(item -> {
            availabilityService.makeFree(item);
            toReturn.set(item);
        });
        return toReturn.get();
    }

    @Override
    public MessageDto findMessageFor(String queue) {
        Assert.notNull(queue, "should never be null");
        Queue q = queueRepository.findByName(queue).orElseThrow(IllegalArgumentException::new);
        if (!availabilityService.checkSessionAvailable(q.getName())) return null;
        Optional<Message> messageCandidate = messageRepository.findFirstByQueueAndStatusInOrderByStartAsc(q, EnumSet.of(Status.ACKNOWLEDGED, Status.ASSIGNED));
        if (messageCandidate.isPresent()) {
            Message message = messageCandidate.get();
            availabilityService.makeBusy(q.getName());
            try {
                return MessageDto.from(message.getQueue().getName(), message.getId(), "do", objectMapper.readTree(message.getPayload()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }
}
