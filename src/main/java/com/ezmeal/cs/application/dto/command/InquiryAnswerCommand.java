package com.ezmeal.cs.application.dto.command;

import com.ezmeal.common.enums.Role;
import java.util.UUID;

public record InquiryAnswerCommand(
        UUID csId,
        String adminId,
        Role role,
        String answer
) {
}
