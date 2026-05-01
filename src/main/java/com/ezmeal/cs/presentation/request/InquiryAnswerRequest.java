package com.ezmeal.cs.presentation.request;

import com.ezmeal.common.enums.Role;
import com.ezmeal.cs.application.dto.command.InquiryAnswerCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record InquiryAnswerRequest(
        @NotBlank(message = "답변 내용은 필수 입력값입니다.")
        String answer
) {
    public InquiryAnswerCommand toCommand(UUID csId, String userId, Role role) {
        return new InquiryAnswerCommand(csId, userId, role, this.answer);
    }
}
