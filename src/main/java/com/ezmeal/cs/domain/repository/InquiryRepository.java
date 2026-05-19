package com.ezmeal.cs.domain.repository;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import com.ezmeal.cs.domain.repository.dto.InquirySearchConditionDto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryRepository {

    // 기본 crud
    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findActiveById(UUID inquiryId);
    Page<Inquiry> searchActiveInquiries(InquirySearchConditionDto condition, Pageable pageable);
    Optional<Inquiry> findActiveByIdAndUserId(UUID inquiryId, String userId);

    // 멱등성 처리 (중복 생성 방지)
    // 특정 기간(초) 이내에 동일한 조건으로 작성된 글이 있는지 확인
    // 네트워크 문제로 인한 중복 요청은 막고, 동일 내용에 대해서도 다시 물어보기 가능
    boolean existsByUserIdAndInquiryTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterAndDeletedAtIsNull(
            String userId,
            InquiryType inquiryType,
            ReferenceType referenceType,
            String referenceId,
            LocalDateTime timeLimit
    );

    // 일괄처리
    void bulkSoftDeleteByUserId(String userId, String deletedBy);
    void bulkUpdateUsernameByUserId(String userId, String newUsername);

}
