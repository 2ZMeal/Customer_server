package com.ezmeal.cs.domain.event.payload.publish;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;

public record InquiryCreatedEvent(
        String csId,
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        LocalDateTime occurredAt
) {
    // Inquiry 객체를 이벤트 페이로드로 바꿔주는 팩토리 메서드
    public static InquiryCreatedEvent from(Inquiry inquiry) {
        return new InquiryCreatedEvent(
                inquiry.getCsId().toString(),
                inquiry.getUserId(),
                inquiry.getInquiryType(),
                inquiry.getReferenceType(),
                inquiry.getReferenceId(),
                inquiry.getTitle(),
                inquiry.getContents(),
                inquiry.getCreatedAt()
        );
    }
}
