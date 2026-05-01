package com.ezmeal.cs.infrastructure.persistence;

import com.ezmeal.cs.domain.enums.AnswerStatus;
import com.ezmeal.cs.domain.enums.InquiryType;
import com.ezmeal.cs.domain.model.Inquiry;
import com.ezmeal.cs.domain.repository.dto.InquirySearchConditionDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import com.ezmeal.cs.domain.enums.ReferenceType;

import static com.ezmeal.cs.domain.model.QInquiry.inquiry;

@Repository
@RequiredArgsConstructor
public class QueryDslInquiryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Inquiry> searchActiveInquiries(InquirySearchConditionDto condition, Pageable pageable) {

        // 데이터 조회 쿼리
        List<Inquiry> content = queryFactory
                .selectFrom(inquiry)
                .where(
                        inquiry.deletedAt.isNull(),
                        userIdEq(condition.userId()),
                        inquiryTypeEq(condition.inquiryType()),
                        referenceTypeEq(condition.referenceType()),
                        referenceIdEq(condition.referenceId()),
                        titleContains(condition.title()),
                        contentsContains(condition.contents()),
                        answerContains(condition.answer()),
                        answerStatusEq(condition.answerStatus())
                )
                // 최신 생성 순 정렬
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(
                        inquiry.deletedAt.isNull(),
                        userIdEq(condition.userId()),
                        inquiryTypeEq(condition.inquiryType()),
                        referenceTypeEq(condition.referenceType()),
                        referenceIdEq(condition.referenceId()),
                        titleContains(condition.title()),
                        contentsContains(condition.contents()),
                        answerContains(condition.answer()),
                        answerStatusEq(condition.answerStatus())
                );

        // PageableExecutionUtils를 사용하여 필요할 때만 카운트 쿼리 실행
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /*
     * ===========================
     * QueryDSL 동적 쿼리용 메서드
     * ===========================
     */

    private BooleanExpression userIdEq(String userId) {
        return StringUtils.hasText(userId) ? inquiry.userId.eq(userId) : null;
    }

    private BooleanExpression inquiryTypeEq(InquiryType inquiryType) {
        return inquiryType != null ? inquiry.inquiryType.eq(inquiryType) : null;
    }

    private BooleanExpression referenceTypeEq(ReferenceType referenceType) {
        return referenceType != null ? inquiry.referenceType.eq(referenceType) : null;
    }

    private BooleanExpression referenceIdEq(String referenceId) {
        return StringUtils.hasText(referenceId) ? inquiry.referenceId.eq(referenceId) : null;
    }

    // 제목 포함 검색
    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? inquiry.title.contains(title) : null;
    }

    // 내용 포함 검색
    private BooleanExpression contentsContains(String contents) {
        return StringUtils.hasText(contents) ? inquiry.contents.contains(contents) : null;
    }

    // 답변 포함 검색
    private BooleanExpression answerContains(String answer) {
        return StringUtils.hasText(answer) ? inquiry.answer.contains(answer) : null;
    }

    private BooleanExpression answerStatusEq(AnswerStatus answerStatus) {
        return answerStatus != null ? inquiry.answerStatus.eq(answerStatus) : null;
    }
}
