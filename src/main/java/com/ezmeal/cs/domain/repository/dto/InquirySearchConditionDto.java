package com.ezmeal.cs.domain.repository.dto;

import com.ezmeal.cs.domain.enums.AnswerStatus;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;

// 문의글 검색 조건을 처리하기 위해 사용
public record InquirySearchConditionDto(
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents,
        String answer,
        AnswerStatus answerStatus
) {}

