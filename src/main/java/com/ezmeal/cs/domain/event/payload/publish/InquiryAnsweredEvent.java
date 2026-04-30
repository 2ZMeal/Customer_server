package com.ezmeal.cs.domain.event.payload.publish;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;

public record InquiryAnsweredEvent(
        String csId,
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        String answer,
        String modifiedBy,
        LocalDateTime modifiedAt
) {
    // Inquiry 객체를 이벤트 페이로드로 바꿔주는 팩토리 메서드
    public static InquiryAnsweredEvent from(Inquiry inquiry) {
        return new InquiryAnsweredEvent(
                inquiry.getCsId().toString(),
                inquiry.getUserId(),
                inquiry.getInquiryType(),
                inquiry.getReferenceType(),
                inquiry.getReferenceId(),
                inquiry.getTitle(),
                inquiry.getContents(),
                inquiry.getAnswer(),
                inquiry.getModifiedBy(),
                inquiry.getModifiedAt() != null ? inquiry.getModifiedAt() : LocalDateTime.now()
        );
    }
}
