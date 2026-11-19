package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

import java.time.LocalDateTime;

// password 필드를 의도적으로 두지 않는다. 엔티티를 그대로 응답하면 비밀번호가 노출된다.
public record MemberDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime updateDate,
        String username,
        String name
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getCreateDate(),
                member.getModifyDate(),
                member.getUsername(),
                member.getName()
        );
    }
}
