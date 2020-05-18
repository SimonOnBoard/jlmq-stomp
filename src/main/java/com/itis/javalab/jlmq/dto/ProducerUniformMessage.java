package com.itis.javalab.jlmq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerUniformMessage<T> {
    private String queue;
    private String option;
    private T payload;
}
