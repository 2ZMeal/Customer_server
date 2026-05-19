package com.ezmeal.cs.domain.event;

import com.ezmeal.cs.domain.event.payload.publish.InquiryAnsweredEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryDeletedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryUpdatedEvent;
import com.ezmeal.cs.domain.model.Inquiry;

public interface InquiryEventProducer {
    // 문의글 생성시
    void publishCreatedEvent(InquiryCreatedEvent event);

    // 문의글 수정시
    void publishUpdatedEvent(InquiryUpdatedEvent event);

    // 문의글 삭제시
    void publishDeletedEvent(InquiryDeletedEvent event);

    // 답변 완료시
    void publishAnsweredEvent(InquiryAnsweredEvent event);
}
