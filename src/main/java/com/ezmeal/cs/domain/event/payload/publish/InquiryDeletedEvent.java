package com.ezmeal.cs.domain.event.payload.publish;

import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;

public record InquiryDeletedEvent(
        String csId,
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        String deletedBy,
        LocalDateTime deletedAt
) implements DomainEvent {
    // Inquiry 객체를 이벤트 페이로드로 바꿔주는 팩토리 메서드
    public static InquiryDeletedEvent from(Inquiry inquiry) {
        return new InquiryDeletedEvent(
                inquiry.getCsId().toString(),
                inquiry.getUserId(),
                inquiry.getInquiryType(),
                inquiry.getReferenceType(),
                inquiry.getReferenceId(),
                inquiry.getTitle(),
                inquiry.getContents(),
                inquiry.getDeletedBy(),
                inquiry.getDeletedAt() != null ? inquiry.getDeletedAt() : LocalDateTime.now()
        );
    }
}
