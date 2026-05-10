package com.ezmeal.cs.infrastructure.message.kafka.consumer;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.common.message.inbox.InboxProcessor;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.application.dto.command.InquiryDeleteCommand;
import com.ezmeal.cs.application.dto.command.InquiryUpdateCommand;
import com.ezmeal.cs.application.service.InquiryService;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryDeletedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryUpdatedEvent;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.UserDeletedEvent;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.UserNameUpdatedEvent;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryEventListenerImpl {

    private final InquiryService inquiryService;
    private final InboxProcessor inboxProcessor;

    // ==================
    // 일반 C, U, D 이벤트
    // ==================

    @KafkaListener(topics = "inquiry-create-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryCreate(EventEnvelope<InquiryCreatedEvent> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if (!"SYSTEM".equals(principal.getUserId())) {
                log.info("[Kafka] 유저({})의 요청으로 문의글을 생성합니다. 문의 타입: {}", principal.getUserId(), envelope.payload().inquiryType());

                InquiryCreateCommand command = new InquiryCreateCommand(
                        principal.getUserId(),
                        envelope.payload().inquiryType(),
                        envelope.payload().referenceType(),
                        envelope.payload().referenceId(),
                        envelope.payload().title(),
                        envelope.payload().contents()
                );
                inquiryService.createInquiry(command);
            } else {
                log.warn("[Kafka] 문의글 생성은 유저 권한이 필수입니다. 시스템 생성을 지원하지 않습니다.");
            }
        });
    }

    @KafkaListener(topics = "inquiry-update-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryUpdate(EventEnvelope<InquiryUpdatedEvent> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if ("SYSTEM".equals(principal.getUserId())) {
                log.warn("[Kafka] 시스템에 의한 단건 문의글 수정은 지원하지 않습니다.");
            } else {
                log.info("[Kafka] 유저({})의 요청으로 문의글({})을/를 수정합니다.", principal.getUserId(), envelope.payload().csId());

                InquiryUpdateCommand command = new InquiryUpdateCommand(
                        UUID.fromString(envelope.payload().csId()),
                        principal.getUserId(),
                        principal.getRole(),
                        envelope.payload().title(),
                        envelope.payload().contents()
                );
                inquiryService.updateInquiry(command);
            }
        });
    }

    @KafkaListener(topics = "inquiry-delete-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryDelete(EventEnvelope<InquiryDeletedEvent> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if ("SYSTEM".equals(principal.getUserId())) {
                log.info("[Kafka] 시스템 요청으로 문의글({})을/를 삭제합니다.", envelope.payload().csId());
                InquiryDeleteCommand systemCommand = new InquiryDeleteCommand(
                        UUID.fromString(envelope.payload().csId()),
                        "SYSTEM",
                        Role.ADMIN
                );
                inquiryService.deleteInquiry(systemCommand);
            } else {
                log.info("[Kafka] 유저({})의 요청으로 문의글({})을/를 삭제합니다.", principal.getUserId(), envelope.payload().csId());
                InquiryDeleteCommand command = new InquiryDeleteCommand(
                        UUID.fromString(envelope.payload().csId()),
                        principal.getUserId(),
                        principal.getRole()
                );
                inquiryService.deleteInquiry(command);
            }
        });
    }

    // ==========================================
    // 일괄 처리 이벤트 (타 서버에서 발생한 이벤트 수신)
    // ==========================================

    @KafkaListener(topics = "${kafka.topic.user.name.updated:user.updated}", groupId = "${spring.kafka.consumer.group-id:customer-group}")
    public void consumeUserNameUpdatedEvent(EventEnvelope<UserNameUpdatedEvent> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            log.info("[Kafka] ({})의 요청으로 이름 일괄 변경을 수행합니다. 대상 유저: {}", principal.getUserId(), envelope.payload().userId());
            inquiryService.bulkUpdateNameByUserId(envelope.payload().userId(), envelope.payload().name());
        });
    }

    @KafkaListener(topics = "${kafka.topic.user.deleted:user.deleted}", groupId = "${spring.kafka.consumer.group-id:customer-group}")
    public void consumeUserDeletedEvent(EventEnvelope<UserDeletedEvent> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            log.info("[Kafka] ({})의 요청으로 유저의 모든 문의글 일괄 삭제를 수행합니다. 대상 유저: {}", principal.getUserId(), envelope.payload().userId());
            inquiryService.bulkSoftDeleteByUserId(envelope.payload().userId(), principal.getUserId());
        });
    }

    // ===================
    // 내부 공통 유틸 메서드
    // ===================

    /**
     * 공통 모듈의 KafkaSecurityInterceptor가 SecurityContext에 넣어둔 유저 정보를 꺼내옵니다.
     * 정보가 없다면 시스템(SYSTEM)의 동작으로 간주합니다.
     */
    private CustomUserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal;
        }

        // 헤더에 인증 정보가 없는 경우 SYSTEM 계정으로 간주
        return new CustomUserPrincipal("SYSTEM", Role.ADMIN, "system@ezmeal.com");
    }
}
