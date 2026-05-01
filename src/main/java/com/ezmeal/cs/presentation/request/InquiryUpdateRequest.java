package com.ezmeal.cs.presentation.request;

import com.ezmeal.common.enums.Role;
import com.ezmeal.cs.application.dto.command.InquiryUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record InquiryUpdateRequest(
        @NotBlank(message = "수정할 제목은 필수 입력값입니다.")
        String title,

        @NotBlank(message = "수정할 내용은 필수 입력값입니다.")
        String contents
) {
    // Controller에서 URL Path Variable(csId)과 Principal(userId, role)을 받아 Command로 변환
    public InquiryUpdateCommand toCommand(UUID csId, String userId, Role role) {
        return new InquiryUpdateCommand(csId, userId, role, this.title, this.contents);
    }
}
