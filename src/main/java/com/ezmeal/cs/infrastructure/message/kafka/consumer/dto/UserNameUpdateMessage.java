package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import java.time.LocalDateTime;

public record UserNameUpdateMessage(
        String userId,
        String userName,
        LocalDateTime occurredAt
) {}
