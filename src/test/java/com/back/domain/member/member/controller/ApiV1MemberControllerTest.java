package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "newUser",
                                            "password": "12345678",
                                            "name": "새유저"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("새유저님 환영합니다. 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        Member member = memberService.findByUsername("newUser").get();
        assertThat(member.getName()).isEqualTo("새유저");
    }

    @Test
    @DisplayName("회원가입, 요청본문 제약 위반(username 너무 짧음)")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "abc",
                                            "password": "12345678",
                                            "name": "새유저"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("회원가입, 실패 - 이미 사용중인 username")
    void t3() throws Exception {
        memberService.join("dupUser", "12345678", "중복유저");

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "dupUser",
                                            "password": "12345678",
                                            "name": "다른유저"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("dupUser(은)는 이미 사용중인 username 입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("내 정보")
    void t4() throws Exception {
        Member actor = memberService.join("meUser", "12345678", "나유저");

        ResultActions resultActions = mvc
                .perform(get("/api/v1/members/me").param("actorId", String.valueOf(actor.getId())))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(actor.getId()))
                .andExpect(jsonPath("$.createDate").exists())
                .andExpect(jsonPath("$.updateDate").exists())
                .andExpect(jsonPath("$.username").value("meUser"))
                .andExpect(jsonPath("$.name").value("나유저"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("내 정보, 실패 - 존재하지 않는 actorId")
    void t5() throws Exception {
        mvc
                .perform(get("/api/v1/members/me").param("actorId", "999999"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("내 정보, 실패 - actorId 없음")
    void t6() throws Exception {
        mvc
                .perform(get("/api/v1/members/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }
}
