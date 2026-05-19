package com.ezmeal.cs.application.dto.command;

import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.enums.ReferenceType;

public record InquiryCreateCommand(
        String userId,
        InquiryType inquiryType,
        ReferenceType referenceType,
        String referenceId,
        String title,
        String contents
) {
}
