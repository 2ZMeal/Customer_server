package com.ezmeal.cs.application.dto.response;

import com.ezmeal.cs.domain.enums.AnswerStatus;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;
import java.util.UUID;

public record InquiryResponse(
        UUID csId,
        String userId,
        String userName,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        AnswerStatus answerStatus,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    // Inquiry 객체를 InquiryResponse DTO로 변환해주는 팩토리 메서드
    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getCsId(),
                inquiry.getUserId(),
                inquiry.getUserName(),
                inquiry.getInquiryType(),
                inquiry.getReferenceType(),
                inquiry.getReferenceId(),
                inquiry.getTitle(),
                inquiry.getContents(),
                inquiry.getAnswerStatus(),
                inquiry.getCreatedAt(),
                inquiry.getModifiedAt()
        );
    }
}
