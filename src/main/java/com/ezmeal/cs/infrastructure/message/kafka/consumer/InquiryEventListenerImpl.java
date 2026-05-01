package com.ezmeal.cs.infrastructure.message.kafka.consumer;

import com.ezmeal.common.enums.Role;
import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.application.dto.command.InquiryDeleteCommand;
import com.ezmeal.cs.application.dto.command.InquiryUpdateCommand;
import com.ezmeal.cs.application.service.InquiryService;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.InquiryCreatedMessage;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.InquiryDeletedMessage;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.InquiryUpdatedMessage;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.UserDeletedMessage;
import com.ezmeal.cs.infrastructure.message.kafka.consumer.dto.UserNameUpdateMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryEventListenerImpl {

    private final InquiryService inquiryService;

    // ==================
    // 일반 C, U, D 이벤트
    // ==================

    // 문의글 생성 메시지 수신
    @KafkaListener(topics = "inquiry-create-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryCreate(
            @Payload InquiryCreatedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if (!"SYSTEM".equals(userId)) {
            log.info("[Kafka] 유저({})의 요청으로 리뷰를 생성합니다. 문의 타입: {}", userId, message.inquiryType());
            InquiryCreateCommand command = message.toCommand(userId);
            // TODO : InquiryService 구현
            // inquiryService.createInquiry(command);
        } else {
            log.warn("[Kafka] 문의글 생성은 유저 권한이 필수입니다. 시스템 생성을 지원하지 않습니다.");
        }
    }

    // 문의글 수정 메시지 수신
    @KafkaListener(topics = "inquiry-update-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryUpdate(
            @Payload InquiryUpdatedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes,
            @Header(value = "X-User-Role", required = false) byte[] roleBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if ("SYSTEM".equals(userId)) {
            log.warn("[Kafka] 시스템에 의한 단건 문의글 수정은 지원하지 않습니다.");
        } else {
            // 일반 유저인 경우에만 엄격한 Role 검사 수행
            Role role = extractRoleHeader(roleBytes);

            log.info("[Kafka] 유저({})의 요청으로 문의글({})을/를 수정합니다.", userId, message.csId());
            InquiryUpdateCommand command = message.toCommand(userId, role);
            // TODO : InquiryService 구현
            // inquiryService.updateReview(command);
        }
    }

    // 문의글 삭제 메시지 수신
    @KafkaListener(topics = "inquiry-delete-command-topic", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void handleInquiryDelete(
            @Payload InquiryDeletedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes,
            @Header(value = "X-User-Role", required = false) byte[] roleBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if ("SYSTEM".equals(userId)) {
            log.info("[Kafka] 시스템 요청으로 문의글({})을/를 삭제합니다.", message.csId());
            InquiryDeleteCommand systemCommand = message.toCommand("SYSTEM", Role.ADMIN);
            // TODO : InquiryService 구현
            // inquiryService.deleteReview(systemCommand);
        } else {
            // 일반 유저인 경우에만 엄격한 Role 검사 수행
            Role role = extractRoleHeader(roleBytes);

            log.info("[Kafka] 유저({})의 요청으로 문의글({})을/를 삭제합니다.", userId, message.csId());
            InquiryDeleteCommand command = message.toCommand(userId, role);
            // TODO : InquiryService 구현
            // inquiryService.deleteReview(command);
        }
    }

    // ==========================================
    // 일괄 처리 이벤트 (타 서버에서 발생한 이벤트 수신)
    // ==========================================

    @KafkaListener(topics = "${kafka.topic.user.name.updated:user-name-updated-topic}", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void consumeUserNameUpdatedEvent(
            @Payload UserNameUpdateMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");
        log.info("[Kafka] ({})의 요청으로 이름 일괄 변경을 수행합니다. 대상 유저: {}", userId, message.userId());
        // TODO : InquiryService 구현
        // inquiryService.bulkUpdateNameByUserId(message.userId(), message.newName());
    }

    @KafkaListener(topics = "${kafka.topic.user.deleted:user-deleted-topic}", groupId = "${spring.kafka.consumer.group-id:inquiry-group}")
    public void consumeUserDeletedEvent(
            @Payload UserDeletedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String deletedBy = extractStringHeader(userIdBytes, "SYSTEM");
        log.info("[Kafka] ({})의 요청으로 유저의 모든 문의글 일괄 삭제를 수행합니다. 대상 유저: {}", deletedBy, message.userId());
        // TODO : InquiryService 구현
        // inquiryService.bulkSoftDeleteByUserId(message.userId(), deletedBy);
    }

    // ===================
    // 내부 공통 유틸 메서드
    // ===================

    // 헤더에서 넘어온 바이트를 문자열로 변환
    private String extractStringHeader(byte[] headerBytes, String defaultValue) {
        if (headerBytes == null || headerBytes.length == 0) {
            return defaultValue;
        }
        return new String(headerBytes, StandardCharsets.UTF_8);
    }

    // 헤더 역할 검증
    private Role extractRoleHeader(byte[] roleBytes) {
        // 헤더가 누락된 경우 (유저 요청인데 Role이 없으면 비정상 접근으로 간주)
        if (roleBytes == null || roleBytes.length == 0) {
            log.error("[Kafka] 필수 헤더인 X-User-Role이 누락되었습니다.");
            throw new IllegalArgumentException("필수 권한 헤더(X-User-Role)가 없습니다.");
        }

        // 헤더 값을 Enum으로 변환
        try {
            String roleStr = new String(roleBytes, StandardCharsets.UTF_8);
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.error("[Kafka] 알 수 없는 Role 헤더 값입니다: {}", new String(roleBytes, StandardCharsets.UTF_8));
            throw new IllegalArgumentException("유효하지 않은 권한 헤더 값입니다.");
        }
    }

}
