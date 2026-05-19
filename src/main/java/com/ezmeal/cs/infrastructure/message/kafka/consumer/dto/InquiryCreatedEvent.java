package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InquiryCreatedEvent(
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents
) implements DomainEvent {
    // Message를 Service에 넘기기 위해 Command로 변환하는 편의 메서드
    public InquiryCreateCommand toCommand(String userId) {
        return new InquiryCreateCommand(
                userId,
                this.inquiryType,
                this.referenceType,
                this.referenceId,
                this.title,
                this.contents
        );
    }
}
