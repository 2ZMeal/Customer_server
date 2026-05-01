package com.ezmeal.cs.domain.model;

import com.ezmeal.common.entity.BaseEntity;
import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.cs.domain.enums.AnswerStatus;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;
import com.ezmeal.cs.domain.exception.InquiryErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_cs", schema = "customer_db")
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cs_id", length = 36, updatable = false, nullable = false)
    private UUID csId;

    @Column(name = "user_id", length = 36, updatable = false, nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", length = 30, updatable = false, nullable = false)
    private InquiryType inquiryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30, updatable = false, nullable = false)
    private ReferenceType referenceType;

    @Column(name = "reference_id", length = 36, updatable = false, nullable = false)
    private String referenceId;

    @Column(name = "title", length = 30, nullable = false)
    private String title;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_status", nullable = false)
    private AnswerStatus answerStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private Inquiry(String userId, String userName, InquiryType inquiryType, ReferenceType referenceType, String referenceId, String title, String contents) {
        this.userId = userId;
        this.userName = userName;
        this.inquiryType = inquiryType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.title = title;
        this.contents = contents;
        this.answerStatus = AnswerStatus.PENDING;
        this.answer = null;
    }

    // 생성
    public static Inquiry create(String userId, String userName, InquiryType inquiryType, ReferenceType referenceType, String referenceId, String title, String contents) {
        validateRequiredString(userId, "userId");
        validateRequiredString(userName, "userName");
        validateNotNull(inquiryType, "inquiryType");
        validateNotNull(referenceType, "referenceType");
        validateRequiredString(referenceId, "referenceId");
        validateRequiredString(title, "title");

        return Inquiry.builder()
                .userId(userId)
                .userName(userName)
                .inquiryType(inquiryType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .title(title)
                .contents(contents)
                .build();
    }

    // 사용자 수정 - 작성자 본인만 제목/내용 수정 가능
    public void updateInquiry(String userId, String title, String contents) {
        if (!this.userId.equals(userId)) {
            throw new ForbiddenException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }

        if (this.answerStatus == AnswerStatus.COMPLETED) {
            throw new BadRequestException(InquiryErrorCode.ALREADY_ANSWERED);
        }

        this.title = title;
        this.contents = contents;
    }

    // 관리자 답변 등록/수정 - 관리자만 answer 업데이트 및 상태 변경
    public void replyInquiry(Role role, String answer) {
        if (role != Role.ADMIN) {
            throw new ForbiddenException(InquiryErrorCode.INQUIRY_FORBIDDEN);
        }

        validateRequiredString(answer, "answer");

        this.answer = answer;
        this.answerStatus = AnswerStatus.COMPLETED;
    }

    // 검증 메서드
    private static void validateRequiredString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }
    }

    // Enum 및 일반 객체 전용 검증 (null 인지만 확인)
    private static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(InquiryErrorCode.INVALID_INPUT);
        }
    }

    // 자신이 작성한 글이거나 관리자인지 검증
    boolean checkRole(String userId, Role role) {
        if (this.userId.equals(userId) || role == Role.ADMIN) {
            return true;
        } else {
            return false;
        }
    }

}
