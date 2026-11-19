# p-14172 연습문제 (70강 ~ 79강)

- 시작 코드: [jhs512/p-14172-2](https://github.com/jhs512/p-14172-2)의 `068` 커밋 시점
- 과제 설명: https://www.slog.gg/p/14172 (4부, 69강 ~ 79강)
- 개발 환경: Java 25, Spring Boot 4.1, Spring Data JPA, H2

## 진행 방식

모든 문제는 **TDD(테스트 주도 개발)** 로 풉니다.

1. **RED**: 원하는 동작을 테스트로 먼저 적습니다. 구현이 없으니 테스트는 실패합니다.
2. **GREEN**: 테스트를 통과하는 최소한의 코드를 작성합니다.
3. 필요하면 테스트를 하나 더 추가(RED)하고 다시 구현(GREEN)합니다.

커밋마다 `./gradlew test` 실행 결과를 이미지로 남겼습니다(`docs/images/`).

## 공통 규칙 (문제 전제)

- 로그인 기능 없이 주소에 `?actorId=회원번호`를 붙여 "누가 요청했는지"를 알립니다.
- actorId가 없거나 존재하지 않는 회원이면 `401` / `{"resultCode": "401-1", "msg": "로그인 후 이용해주세요."}`
- 본인 글/댓글이 아닌데 수정·삭제하려 하면 `403` / `{"resultCode": "403-1", "msg": "권한이 없습니다."}`

---

## 문제 1: 회원가입 API

`POST /api/v1/members/join`

### 커밋 1: 테스트 먼저 작성 (RED)

**변경 파일**: `src/test/java/com/back/domain/member/member/controller/ApiV1MemberControllerTest.java` (새 파일)

- `t1` 회원가입 성공: 응답 상태 `201`, `resultCode` `201-1`, 메시지 `새유저님 환영합니다. 회원가입이 완료되었습니다.`를 기대합니다. 가입 후 DB에서 `findByUsername`으로 회원을 다시 찾아 이름이 저장됐는지도 확인합니다.
- `t2` 요청 본문 제약 위반: username이 3글자(`abc`)면 `Size(4, 30)` 위반으로 `400-1`이 나와야 합니다.
- 테스트 클래스의 어노테이션
  - `@ActiveProfiles("test")`: 메모리 DB를 쓰는 테스트 설정을 사용합니다.
  - `@SpringBootTest`: 스프링 앱 전체를 띄워서 테스트합니다.
  - `@AutoConfigureMockMvc`: 서버를 실제로 띄우지 않고 HTTP 요청을 흉내 내는 `MockMvc`를 준비합니다.
  - `@Transactional`: 테스트가 끝나면 DB 변경을 롤백해서 테스트끼리 서로 영향을 주지 않게 합니다.

**결과**: `Member`, `MemberService`, `ApiV1MemberController`가 아직 없어서 컴파일부터 실패합니다(RED).

![커밋 1 테스트 결과](docs/images/01.png)
