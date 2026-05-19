package com.ezmeal.cs.infrastructure.persistence;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import com.ezmeal.cs.domain.repository.InquiryRepository;
import com.ezmeal.cs.domain.repository.dto.InquirySearchConditionDto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepository {

    private final JpaInquiryRepository jpaInquiryRepository;
    private final QueryDslInquiryRepository queryDslInquiryRepository;

    /*
     * ===================
     * JPA Repository 구역
     * ===================
     * */

    // 저장
    @Override
    public Inquiry save(Inquiry inquiry) {
        return jpaInquiryRepository.save(inquiry);
    }

    // 단건 조회
    @Override
    public Optional<Inquiry> findActiveById(UUID inquiryId) {
        return jpaInquiryRepository.findActiveById(inquiryId);
    }

    // csId와 userId로 동시 조회
    @Override
    public Optional<Inquiry> findActiveByIdAndUserId(UUID inquiryId, String userId) {
        return jpaInquiryRepository.findActiveByIdAndUserId(inquiryId, userId);
    }

    // 멱등성 검사 (네트워크 문제로 인한 따닥 방지용)
    @Override
    public boolean existsByUserIdAndInquiryTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterAndDeletedAtIsNull(
            String userId, InquiryType inquiryType, ReferenceType referenceType, String referenceId,
            LocalDateTime timeLimit) {
        return jpaInquiryRepository.existsByUserIdAndInquiryTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterAndDeletedAtIsNull(
                userId, inquiryType, referenceType, referenceId, timeLimit
        );
    }

    // 사용자 탈퇴시 일괄 삭제 (Soft Delete)
    @Override
    public void bulkSoftDeleteByUserId(String userId, String deletedBy) {
        jpaInquiryRepository.bulkSoftDeleteByUserId(userId, deletedBy);
    }

    // 사용자 이름 변경 시 일괄 변경
    @Override
    public void bulkUpdateUsernameByUserId(String userId, String newUsername) {
        jpaInquiryRepository.bulkUpdateUsernameByUserId(userId, newUsername);
    }

    /*
     * ==============
     * Query DSL 구역
     * ==============
     * */

    // 필터에 따른 조건별 검색
    @Override
    public Page<Inquiry> searchActiveInquiries(InquirySearchConditionDto condition, Pageable pageable) {
        return queryDslInquiryRepository.searchActiveInquiries(condition, pageable);
    }

}
