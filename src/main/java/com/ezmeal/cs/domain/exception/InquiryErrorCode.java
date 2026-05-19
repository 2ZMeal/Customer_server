package com.ezmeal.cs.domain.exception;

import com.ezmeal.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "CUSTOMER_SERVICE_400_1", "필수 입력값 검증에 실패하였습니다."),
    ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "CUSTOMER_SERVICE_400_2", "이미 답변이 완료된 문의입니다."),

    INQUIRY_FORBIDDEN(HttpStatus.FORBIDDEN, "CUSTOMER_SERVICE_403_1", "글을 수정하거나 삭제할 권한이 없습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_SERVICE_404_1", "해당 글을 찾을 수 없습니다."),
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "CUSTOMER_SERVICE_409_1", "연속된 요청입니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
