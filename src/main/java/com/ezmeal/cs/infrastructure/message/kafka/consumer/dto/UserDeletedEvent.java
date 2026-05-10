package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.message.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDeletedEvent(
        String userId,
        LocalDateTime occurredAt
) implements DomainEvent {}
