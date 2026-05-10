package com.ezmeal.cs.domain.event.payload.publish;

import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;

public record InquiryUpdatedEvent(
        String csId,
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        String modifiedBy,
        LocalDateTime modifiedAt
) implements DomainEvent {
    // Inquiry 객체를 이벤트 페이로드로 바꿔주는 팩토리 메서드
    public static InquiryUpdatedEvent from(Inquiry inquiry) {
        return new InquiryUpdatedEvent(
                inquiry.getCsId().toString(),
                inquiry.getUserId(),
                inquiry.getInquiryType(),
                inquiry.getReferenceType(),
                inquiry.getReferenceId(),
                inquiry.getTitle(),
                inquiry.getContents(),
                inquiry.getModifiedBy(),
                inquiry.getModifiedAt() != null ? inquiry.getModifiedAt() : LocalDateTime.now()
        );
    }
}

