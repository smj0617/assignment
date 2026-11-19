package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BaseInitDataTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;

    @Test
    @DisplayName("샘플 데이터: 회원 6명이 생성된다")
    void t1() {
        String[][] expected = {
                {"system", "시스템"},
                {"admin", "관리자"},
                {"user1", "유저1"},
                {"user2", "유저2"},
                {"user3", "유저3"},
                {"user4", "유저4"},
        };

        for (String[] row : expected) {
            Member member = memberService.findByUsername(row[0]).get();

            assertThat(member.getName()).isEqualTo(row[1]);
            assertThat(member.getPassword()).isEqualTo("12345678");
        }
    }

    @Test
    @DisplayName("샘플 데이터: 글과 댓글은 작성자(author)를 가진다")
    void t2() {
        List<Post> posts = postService.findAll();

        assertThat(posts.get(0).getAuthor().getUsername()).isEqualTo("user1");
        assertThat(posts.get(1).getAuthor().getUsername()).isEqualTo("user2");
        assertThat(posts.get(2).getAuthor().getUsername()).isEqualTo("user3");

        List<PostComment> comments = new ArrayList<>();
        comments.addAll(posts.get(0).getComments());
        comments.addAll(posts.get(1).getComments());

        assertThat(comments)
                .extracting(comment -> comment.getAuthor().getUsername())
                .containsExactly("user1", "user2", "user3", "user1", "user2");
    }

    @Test
    @DisplayName("글/댓글 조회 응답에 작성자 정보가 포함된다")
    void t3() throws Exception {
        Post post = postService.findAll().get(0);
        PostComment comment = post.getComments().get(0);

        mvc.perform(get("/api/v1/posts/%d".formatted(post.getId())))
                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value("유저1"));

        mvc.perform(get("/api/v1/posts/%d/comments/%d".formatted(post.getId(), comment.getId())))
                .andExpect(jsonPath("$.authorId").value(comment.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value("유저1"));
    }
}
