package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.AccessDeniedException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Post extends BaseEntity {
    // 작성자. 회원 1명이 글 여러 개를 쓸 수 있으므로 N:1(ManyToOne) 관계
    @ManyToOne(fetch = LAZY)
    private Member author;
    private String title;
    private String content;

    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<PostComment> comments = new ArrayList<>();

    public Post(Member author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public PostComment addComment(Member author, String content) {
        PostComment postComment = new PostComment(author, this, content);
        comments.add(postComment);

        return postComment;
    }

    public Optional<PostComment> findCommentById(int id) {
        return comments
                .stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    // 삭제 권한 검사: 작성자 본인이 아니면 AccessDeniedException(403)
    public void checkActorCanDelete(Member actor) {
        // 객체(equals) 대신 id로 비교: author는 LAZY 프록시일 수 있어 클래스 비교가 어긋날 수 있다
        if (author.getId() != actor.getId()) throw new AccessDeniedException();
    }

    // 수정 권한 검사: 지금은 삭제와 규칙이 같지만, 나중에 달라질 수 있어 메서드를 따로 둔다
    public void checkActorCanModify(Member actor) {
        if (author.getId() != actor.getId()) throw new AccessDeniedException();
    }

    public boolean deleteComment(PostComment postComment) {
        if (postComment == null) return false;

        return comments.remove(postComment);
    }
}