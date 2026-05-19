package com.ezmeal.cs.presentation.request;

import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InquiryCreateRequest(
        @NotNull(message = "문의 타입은 필수 입력값입니다.")
        InquiryType inquiryType,

        @NotNull(message = "참조 타입은 필수 입력값입니다.")
        ReferenceType referenceType,

        @NotBlank(message = "참조 ID는 필수 입력값입니다.")
        String referenceId,

        @NotBlank(message = "제목은 필수 입력값입니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력값입니다.")
        String contents
) {
    // Request를 Service에 넘기기 위해 Command로 변환하는 편의 메서드
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
