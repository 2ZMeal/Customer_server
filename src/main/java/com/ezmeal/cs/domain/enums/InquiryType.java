package com.ezmeal.cs.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryType {
    DELIVERY("배달 문의"),
    PRODUCT("상품 문의"),
    USER("회원 문의"),
    COMPANY("업체 문의"),
    PAYMENT("결제 문의"),
    ORDER("주문 문의"),
    EXTRA("기타 문의");

    private final String description;
}
