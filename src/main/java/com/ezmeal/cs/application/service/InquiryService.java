package com.ezmeal.cs.application.service;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.cs.application.dto.command.InquiryAnswerCommand;
import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.application.dto.command.InquiryDeleteCommand;
import com.ezmeal.cs.application.dto.command.InquiryUpdateCommand;
import com.ezmeal.cs.application.dto.response.InquiryResponse;
import com.ezmeal.cs.domain.event.InquiryEventProducer;
import com.ezmeal.cs.domain.event.payload.publish.InquiryAnsweredEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryDeletedEvent;
import com.ezmeal.cs.domain.event.payload.publish.InquiryUpdatedEvent;
import com.ezmeal.cs.domain.exception.InquiryErrorCode;
import com.ezmeal.cs.domain.model.Inquiry;
import com.ezmeal.cs.domain.provider.UserData;
import com.ezmeal.cs.domain.provider.UserProvider;
import com.ezmeal.cs.domain.repository.InquiryRepository;
import com.ezmeal.cs.domain.repository.dto.InquirySearchConditionDto;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryEventProducer inquiryEventProducer;
    private final UserProvider userProvider;
    private final TransactionTemplate transactionTemplate;

    // ==============================================
    // API로 호출하는 경우와 이벤트를 수신했을 때를 모두 대응
    // ==============================================

    // 문의글 생성 (외부 API 통신이 있으므로 TransactionTemplate 사용)
    public InquiryResponse createInquiry(InquiryCreateCommand command) {

        // Feign Client 호출 (DB 커넥션을 물고 있지 않음)
        UserData userData = userProvider.getUser(command.userId());

        Inquiry savedInquiry;
        try {
            // 비즈니스 로직 + Outbox 이벤트 발행을 하나의 트랜잭션으로 묶음
            savedInquiry = transactionTemplate.execute(status -> {

                // 멱등성 검사 -> 10초 이내에 동일한 참조 정보로 작성된 문의가 있는지 확인 (더블클릭 방지)
                LocalDateTime timeLimit = LocalDateTime.now().minusSeconds(10);
                boolean isDuplicate = inquiryRepository.existsByUserIdAndInquiryTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterAndDeletedAtIsNull(
                        command.userId(),
                        command.inquiryType(),
                        command.referenceType(),
                        command.referenceId(),
                        timeLimit
                );

                if (isDuplicate) {
                    log.warn("중복 문의 작성 요청 발생 (10초 이내): userId={}", command.userId());
                    throw new BadRequestException(InquiryErrorCode.DUPLICATE_REQUEST);
                }

                Inquiry inquiry = Inquiry.create(
                        command.userId(),
                        userData.userName(),
                        command.inquiryType(),
                        command.referenceType(),
                        command.referenceId(),
                        command.title(),
                        command.contents()
                );

                Inquiry saved = inquiryRepository.save(inquiry);

                // 트랜잭션이 끝나기 전에 이벤트 발행 (Outbox DB에 INIT 상태로 함께 저장)
                inquiryEventProducer.publishCreatedEvent(InquiryCreatedEvent.from(saved));

                return saved;
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("동시 문의 작성 요청 발생: userId={}", command.userId());
            throw new BadRequestException(InquiryErrorCode.DUPLICATE_REQUEST);
        }

        return InquiryResponse.from(savedInquiry);
    }

    // 문의글 수정 (외부 통신이 없으므로 @Transactional 사용)
    @Transactional
    public InquiryResponse updateInquiry(InquiryUpdateCommand command) {
        Inquiry inquiry = inquiryRepository.findActiveByIdAndUserId(command.csId(), command.userId())
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.updateInquiry(command.userId(), command.title(), command.contents());

        // 트랜잭션 내부에서 이벤트 발행 (Outbox 연동)
        inquiryEventProducer.publishUpdatedEvent(InquiryUpdatedEvent.from(inquiry));

        return InquiryResponse.from(inquiry);
    }

    // 관리자 답변 등록/수정 (외부 통신이 없으므로 @Transactional 사용)
    @Transactional
    public InquiryResponse answerInquiry(InquiryAnswerCommand command) {
        if (command.role() != Role.ADMIN) {
            throw new ForbiddenException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }

        Inquiry inquiry = inquiryRepository.findActiveById(command.csId())
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.replyInquiry(command.role(), command.answer());

        // 트랜잭션 내부에서 이벤트 발행 (Outbox 연동)
        inquiryEventProducer.publishAnsweredEvent(InquiryAnsweredEvent.from(inquiry));

        return InquiryResponse.from(inquiry);
    }

    // 문의글 삭제 (외부 통신이 없으므로 @Transactional 사용)
    @Transactional
    public void deleteInquiry(InquiryDeleteCommand command) {
        // 관리자는 전체글 조회 가능, 일반 유저는 본인 글만 조회 (IDOR 고려)
        Inquiry inquiry = command.role() == Role.ADMIN
                ? inquiryRepository.findActiveById(command.csId())
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND))
                : inquiryRepository.findActiveByIdAndUserId(command.csId(), command.userId())
                        .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getDeletedAt() == null) {
            inquiry.delete(command.userId());

            // 트랜잭션 내부에서 이벤트 발행 (Outbox 연동)
            inquiryEventProducer.publishDeletedEvent(InquiryDeletedEvent.from(inquiry));
        } else {
            log.info("이미 삭제 처리된 문의글입니다. csId: {}", command.csId());
        }
    }

    // =============================================
    // 단순 조회 메서드들 (@Transactional(readOnly = true))
    // =============================================

    // 단건 문의 상세 조회
    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(UUID csId) {
        Inquiry inquiry = inquiryRepository.findActiveById(csId)
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));
        return InquiryResponse.from(inquiry);
    }

    // 다건 조건 검색 및 페이징 조회
    @Transactional(readOnly = true)
    public Page<InquiryResponse> searchInquiries(InquirySearchConditionDto condition, Pageable pageable) {
        // QueryDSL 레포지토리를 통해 엔티티 Page 조회
        Page<Inquiry> inquiryPage = inquiryRepository.searchActiveInquiries(condition, pageable);
        // 엔티티를 DTO로 변환하여 반환 (Page 인터페이스의 map 활용)
        return inquiryPage.map(InquiryResponse::from);
    }


    // =============================================
    // 일괄 처리 로직 (외부 통신 없으므로 @Transactional로 리팩토링)
    // =============================================

    // 회원 이름 변경 시 일괄 변경 (Kafka 이벤트 수신용)
    @Transactional
    public void bulkUpdateNameByUserId(String userId, String newName) {

        if (userId == null || userId.isBlank() || newName == null || newName.isBlank()) {
            log.warn("일괄 이름 변경 실패: 잘못된 입력값입니다. userId={}", userId);
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }

        inquiryRepository.bulkUpdateUsernameByUserId(userId, newName);
        log.info("유저({})의 모든 문의글 이름이[ {} ] 로 일괄 변경되었습니다.", userId, newName);
    }

    // 유저 탈퇴 시 해당 유저의 문의 일괄 삭제 (Kafka 이벤트 수신용)
    @Transactional
    public void bulkSoftDeleteByUserId(String userId, String deletedBy) {

        if (userId == null || userId.isBlank() || deletedBy == null || deletedBy.isBlank()) {
            log.warn("일괄 삭제 처리 실패: 필수 입력값이 누락되었습니다. userId={}", userId);
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }

        inquiryRepository.bulkSoftDeleteByUserId(userId, deletedBy);
        log.info("유저({}) 탈퇴로 인해 작성한 모든 문의가 삭제 처리되었습니다. (deletedBy={})", userId, deletedBy);
    }

}
