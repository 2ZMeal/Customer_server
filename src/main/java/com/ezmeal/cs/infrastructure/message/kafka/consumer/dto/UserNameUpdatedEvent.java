package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.message.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserNameUpdatedEvent(
        String userId,
        String name,
        LocalDateTime occurredAt
) implements DomainEvent {}
