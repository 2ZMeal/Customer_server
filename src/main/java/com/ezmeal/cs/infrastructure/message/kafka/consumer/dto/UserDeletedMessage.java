package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDeletedMessage(
        String userId,
        LocalDateTime occurredAt
) {}
