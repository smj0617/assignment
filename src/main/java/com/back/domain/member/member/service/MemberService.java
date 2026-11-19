package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.MemberDuplicateUsernameException;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public long count() {
        return memberRepository.count();
    }

    public Member join(String username, String password, String name) {
        memberRepository.findByUsername(username)
                .ifPresent(_ -> {
                    throw new MemberDuplicateUsernameException(username);
                });

        Member member = new Member(username, password, name);

        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    // actorId가 필요한 모든 액션에서 쓰는 공통 진입점.
    // actorId가 없거나(null) 해당 회원이 없으면 401로 응답되도록 UnauthenticatedException을 던진다.
    public Member getActor(Integer actorId) {
        if (actorId == null) throw new UnauthenticatedException();

        return memberRepository.findById(actorId)
                .orElseThrow(UnauthenticatedException::new);
    }
}
