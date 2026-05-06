package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserNameUpdateMessage(
        String userId,

        @JsonProperty("name")
        String userName,

        LocalDateTime occurredAt
) {}
