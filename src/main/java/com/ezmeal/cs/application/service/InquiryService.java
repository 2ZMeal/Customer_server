package com.ezmeal.cs.application.service;

import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.cs.application.dto.command.InquiryCreateCommand;
import com.ezmeal.cs.application.dto.response.InquiryResponse;
import com.ezmeal.cs.domain.event.InquiryEventProducer;
import com.ezmeal.cs.domain.event.payload.publish.InquiryCreatedEvent;
import com.ezmeal.cs.domain.exception.InquiryErrorCode;
import com.ezmeal.cs.domain.model.Inquiry;
import com.ezmeal.cs.domain.provider.UserData;
import com.ezmeal.cs.domain.provider.UserProvider;
import com.ezmeal.cs.domain.repository.InquiryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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

}
