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

    /*
    Feign Client가 @Transaction 내부에서 가져오는 경우면
    DB 커넥션 풀이 고갈될 수 있음 (가져오는게 늦어지는 경우에)
    이를 위해 transaction 범위를 세밀하게 지정할 수 있는 transactionTemplate를 사용
     */
    private final TransactionTemplate transactionTemplate;

    // ==============================================
    // API로 호출하는 경우와 이벤트를 수신했을 때를 모두 대응
    // ==============================================

    // 문의글 생성
    public InquiryResponse createInquiry(InquiryCreateCommand command) {

        UserData userData = userProvider.getUser(command.userId());

        Inquiry savedInquiry;
        try {
            // 트랜잭션 내부
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

                return inquiryRepository.save(inquiry);
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("동시 문의 작성 요청 발생: userId={}", command.userId());
            throw new BadRequestException(InquiryErrorCode.DUPLICATE_REQUEST);
        }

        // 이벤트 발행
        inquiryEventProducer.publishCreatedEvent(InquiryCreatedEvent.from(savedInquiry));

        return InquiryResponse.from(savedInquiry);
    }

    // 문의글 수정 (문의 내용 수정)
    public InquiryResponse updateInquiry(InquiryUpdateCommand command) {
        Inquiry updatedInquiry = transactionTemplate.execute(status -> {
            Inquiry inquiry = inquiryRepository.findActiveByIdAndUserId(command.csId(), command.userId())
                    .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

            inquiry.updateInquiry(command.userId(), command.title(), command.contents());
            return inquiry;
        });

        inquiryEventProducer.publishUpdatedEvent(InquiryUpdatedEvent.from(updatedInquiry));
        return InquiryResponse.from(updatedInquiry);
    }

    // 관리자 답변 등록/수정
    public InquiryResponse answerInquiry(InquiryAnswerCommand command) {
        if (command.role() != Role.ADMIN) {
            throw new ForbiddenException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }

        Inquiry answeredInquiry = transactionTemplate.execute(status -> {
            Inquiry inquiry = inquiryRepository.findActiveById(command.csId())
                    .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

            inquiry.replyInquiry(command.role(), command.answer());
            return inquiry;
        });

        // 이벤트 발행
        inquiryEventProducer.publishAnsweredEvent(InquiryAnsweredEvent.from(answeredInquiry));
        return InquiryResponse.from(answeredInquiry);
    }

    // 회원 이름 변경 시 일괄 변경 (Kafka 이벤트 수신용)
    public void bulkUpdateNameByUserId(String userId, String newName) {

        if (userId == null || userId.isBlank() || newName == null || newName.isBlank()) {
            log.warn("일괄 이름 변경 실패: 잘못된 입력값입니다. userId={}", userId);
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }

        transactionTemplate.executeWithoutResult(status -> {
            inquiryRepository.bulkUpdateUsernameByUserId(userId, newName);
        });
        log.info("유저({})의 모든 문의글 이름이 ( {} ) 로 일괄 변경되었습니다.", userId, newName);
    }

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

    // 문의글 삭제 (작성자 또는 관리자)
    public void deleteInquiry(InquiryDeleteCommand command) {
        Inquiry deletedInquiry = transactionTemplate.execute(status -> {

            // 관리자는 전체글 조회 가능, 일반 유저는 본인 글만 조회 (IDOR 고려)
            Inquiry inquiry = command.role() == Role.ADMIN
                    ? inquiryRepository.findActiveById(command.csId())
                    .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND))
                    : inquiryRepository.findActiveByIdAndUserId(command.csId(), command.userId())
                            .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

            inquiry.delete(command.userId());

            return inquiry;
        });

        // 이벤트 발행
        inquiryEventProducer.publishDeletedEvent(InquiryDeletedEvent.from(deletedInquiry));
    }

    // 유저 탈퇴 시 해당 유저의 문의 일괄 삭제 (Kafka 이벤트 수신용)
    public void bulkSoftDeleteByUserId(String userId, String deletedBy) {

        if (userId == null || userId.isBlank() || deletedBy == null || deletedBy.isBlank()) {
            log.warn("일괄 삭제 처리 실패: 필수 입력값이 누락되었습니다. userId={}", userId);
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }

        transactionTemplate.executeWithoutResult(status -> {
            inquiryRepository.bulkSoftDeleteByUserId(userId, deletedBy);
        });
        log.info("유저({}) 탈퇴로 인해 작성한 모든 문의가 삭제 처리되었습니다. (deletedBy={})", userId, deletedBy);
    }

}
