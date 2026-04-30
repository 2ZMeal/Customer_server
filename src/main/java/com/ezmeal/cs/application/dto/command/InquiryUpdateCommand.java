package com.ezmeal.cs.application.dto.command;

import com.ezmeal.common.enums.Role;
import java.util.UUID;

public record InquiryUpdateCommand(
        UUID csId,
        String userId,
        Role role,
        String title,
        String contents
) {
}
