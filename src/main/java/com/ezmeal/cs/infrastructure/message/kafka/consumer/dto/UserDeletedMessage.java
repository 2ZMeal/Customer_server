package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import java.time.LocalDateTime;

public record UserDeletedMessage(
        String userId,
        String deletedBy,
        LocalDateTime occurredAt
) {}
