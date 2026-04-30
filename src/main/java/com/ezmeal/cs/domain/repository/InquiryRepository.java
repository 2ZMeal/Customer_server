package com.ezmeal.cs.domain.repository;

import com.ezmeal.cs.domain.model.Inquiry;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryRepository {

    // 기본 crud
    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findActiveById(UUID id);
    Page<Inquiry> searchActivateInquiries(Pageable pageable);

    // 멱등성 처리 (중복 생성 방지)

    // 일괄처리

}
