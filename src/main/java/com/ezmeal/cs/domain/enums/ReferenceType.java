package com.ezmeal.cs.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReferenceType {
    SHIPMENT("배달"),
    NOTIFICATION("알림"),
    REVIEW("리뷰"),
    PRODUCT("상품"),
    COMPANY("업체"),
    CS("고객센터"),
    PAYMENT("결제"),
    ORDER("주문"),
    CART("장바구니"),
    USER("회원");

    private final String description;
}
