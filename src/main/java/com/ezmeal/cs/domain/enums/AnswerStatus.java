package com.ezmeal.cs.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerStatus {
    PENDING("응답 준비중"),
    COMPLETED("응답 완료");

    private final String description;
}
