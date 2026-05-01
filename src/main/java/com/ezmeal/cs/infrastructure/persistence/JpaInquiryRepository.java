package com.ezmeal.cs.infrastructure.persistence;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.model.Inquiry;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaInquiryRepository extends JpaRepository<Inquiry, UUID> {

    // 단건 조회 (Soft Delete 되지 않은 데이터만)
    @Query("SELECT i FROM Inquiry i WHERE i.csId = :id AND i.deletedAt IS NULL")
    Optional<Inquiry> findActiveById(@Param("id") UUID id);

    // 멱등성(중복 생성 방지)을 위한 검증
    // 특정 시간(timeLimit) 이후 내에 (ex - 3초 이내) 동일한 유저가 동일한 타입과 레퍼런스로 작성한 글이 있는지 확인
    boolean existsByUserIdAndInquiryTypeAndReferenceTypeAndReferenceIdAndCreatedAtAfterAndDeletedAtIsNull(
            String userId,
            InquiryType inquiryType,
            ReferenceType referenceType,
            String referenceId,
            LocalDateTime timeLimit
    );

    // 회원 탈퇴 시 해당 회원이 작성한 모든 문의글 삭제 (Soft Delete)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inquiry i " +
            "SET i.deletedAt = CURRENT_TIMESTAMP, i.deletedBy = :deletedBy " +
            "WHERE i.userId = :userId AND i.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(@Param("userId") String userId, @Param("deletedBy") String deletedBy);

    // 사용자 이름 변경 시 해당 사용자의 문의글에 있는 이름을 일괄 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Inquiry i " +
            "SET i.userName = :newUsername " +
            "WHERE i.userId = :userId AND i.deletedAt IS NULL")
    void bulkUpdateUsernameByUserId(@Param("userId") String userId, @Param("newUsername") String newUsername);

}
