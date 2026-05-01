package com.ezmeal.cs.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.enums.Role;
import com.ezmeal.cs.application.dto.command.InquiryAnswerCommand;
import java.util.UUID;

public record InquiryAnsweredMessage(
        UUID csId,
        String answer
) {
    // Message와 카프카 헤더에서 추출한 인증 정보를 조합해 비즈니스 Command로 변환
    public InquiryAnswerCommand toCommand(String userId, Role role) {
        return new InquiryAnswerCommand(
                this.csId,
                userId,
                role,
                this.answer
        );
    }
}
