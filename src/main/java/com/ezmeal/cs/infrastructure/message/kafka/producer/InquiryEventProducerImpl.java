package com.ezmeal.cs.infrastructure.message.kafka.producer;

import com.ezmeal.common.message.CommonKafkaEventPublisher;
import com.ezmeal.cs.domain.event.InquiryEventProducer;
import com.ezmeal.cs.domain.event.payload.publish.InquiryAnsweredEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryDeletedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryEventProducerImpl implements InquiryEventProducer {

    private final CommonKafkaEventPublisher eventPublisher;

    @Value("${kafka.topic.inquiry.created:inquiry-created-topic}")
    private String inquiryCreatedTopic;

    @Value("${kafka.topic.inquiry.updated:inquiry-updated-topic}")
    private String inquiryUpdatedTopic;

    @Value("${kafka.topic.inquiry.deleted:inquiry-deleted-topic}")
    private String inquiryDeletedTopic;

    @Value("${kafka.topic.inquiry.answered:inquiry-answered-topic}")
    private String inquiryAnsweredTopic;

    @Override
    public void publishCreatedEvent(InquiryCreatedEvent event) {
        eventPublisher.publish(
                inquiryCreatedTopic,
                event.csId(),           // aggregateId (순서 보장용 기준 키)
                "INQUIRY_CREATED",      // eventType
                event                   // payload (DomainEvent 구현체)
        );
    }

    @Override
    public void publishUpdatedEvent(InquiryUpdatedEvent event) {
        eventPublisher.publish(
                inquiryUpdatedTopic,
                event.csId(),
                "INQUIRY_UPDATED",
                event
        );
    }

    @Override
    public void publishDeletedEvent(InquiryDeletedEvent event) {
        eventPublisher.publish(
                inquiryDeletedTopic,
                event.csId(),
                "INQUIRY_DELETED",
                event
        );
    }

    @Override
    public void publishAnsweredEvent(InquiryAnsweredEvent event) {
        eventPublisher.publish(
                inquiryAnsweredTopic,
                event.csId(),
                "INQUIRY_ANSWERED",
                event
        );
    }
}
