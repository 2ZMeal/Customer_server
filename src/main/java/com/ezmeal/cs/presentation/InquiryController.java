package com.ezmeal.cs.presentation;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.cs.application.dto.response.InquiryResponse;
import com.ezmeal.cs.application.service.InquiryService;
import com.ezmeal.cs.presentation.request.InquiryAnswerRequest;
import com.ezmeal.cs.presentation.request.InquiryCreateRequest;
import com.ezmeal.cs.presentation.request.InquiryUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cs")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의글 생성
    @PostMapping
    public ResponseEntity<CommonApiResponse<InquiryResponse>> createInquiry(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody InquiryCreateRequest request
    ) {
        InquiryResponse response = inquiryService.createInquiry(request.toCommand(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.success(response));
    }

    // 문의글 수정 (작성자 본인)
    @PatchMapping("/{csId}")
    public ResponseEntity<CommonApiResponse<InquiryResponse>> updateInquiry(
            @PathVariable("csId") UUID csId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody InquiryUpdateRequest request
    ) {
        InquiryResponse response = inquiryService.updateInquiry(
                request.toCommand(csId, principal.getUserId(), principal.getRole())
        );
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    // 관리자 답변 등록/수정
    @PatchMapping("/{csId}/answer")
    public ResponseEntity<CommonApiResponse<InquiryResponse>> answerInquiry(
            @PathVariable("csId") UUID csId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody InquiryAnswerRequest request
    ) {
        InquiryResponse response = inquiryService.answerInquiry(
                request.toCommand(csId, principal.getUserId(), principal.getRole())
        );
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

}
