package com.ezmeal.cs.infrastructure.message.kafka.producer;

import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.cs.domain.event.InquiryEventProducer;
import com.ezmeal.cs.domain.event.payload.publish.InquiryAnsweredEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryDeletedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryUpdatedEvent;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryEventProducerImpl implements InquiryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(inquiryCreatedTopic, event.csId(), event);
    }

    @Override
    public void publishUpdatedEvent(InquiryUpdatedEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(inquiryUpdatedTopic, event.csId(), event);
    }

    @Override
    public void publishDeletedEvent(InquiryDeletedEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(inquiryDeletedTopic, event.csId(), event);
    }

    @Override
    public void publishAnsweredEvent(InquiryAnsweredEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(inquiryAnsweredTopic, event.csId(), event);
    }

    // 카프카 이벤트 헤더에 사용자 정보 담기
    private void sendWithHeaders(String topic, String key, Object payload) {
        // 토픽, 키, 페이로드를 담은 record를 생성
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, payload);

        // 현재 스레드의 인증 정보(SecurityContext)를 가져옴
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 있는 유저의 요청일 경우에만 카프카 헤더 추가
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            record.headers().add("X-User-Id", principal.getUserId().getBytes(StandardCharsets.UTF_8));
            record.headers().add("X-User-Roles", principal.getRole().name().getBytes(StandardCharsets.UTF_8));
        }

        // 카프카 전송
        kafkaTemplate.send(record);
    }
}
