package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.AccessDeniedException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    // 댓글 작성자. 회원 1명이 댓글 여러 개를 쓸 수 있으므로 N:1(ManyToOne) 관계
    @ManyToOne(fetch = LAZY)
    private Member author;
    @ManyToOne(fetch = LAZY)
    private Post post;
    private String content;

    public PostComment(Member author, Post post, String content) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    // 삭제 권한 검사: 댓글 작성자 본인이 아니면 403 (글의 checkActorCanDelete와 같은 방식)
    public void checkActorCanDelete(Member actor) {
        if (author.getId() != actor.getId()) throw new AccessDeniedException();
    }

    // 수정 권한 검사: 댓글 작성자 본인이 아니면 403
    public void checkActorCanModify(Member actor) {
        if (author.getId() != actor.getId()) throw new AccessDeniedException();
    }

    public void modify(String content) {
        this.content = content;
    }
}