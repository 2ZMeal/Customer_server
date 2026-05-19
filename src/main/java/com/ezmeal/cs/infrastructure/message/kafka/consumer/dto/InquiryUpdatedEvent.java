package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.cs.application.dto.command.InquiryUpdateCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InquiryUpdatedEvent(
        UUID csId,
        String title,
        String contents
) implements DomainEvent {
    // Message와 카프카 헤더에서 추출한 인증 정보를 조합해 비즈니스 Command로 변환
    public InquiryUpdateCommand toCommand(String userId, Role role) {
        return new InquiryUpdateCommand(
                this.csId,
                userId,
                role,
                this.title,
                this.contents
        );
    }
}
