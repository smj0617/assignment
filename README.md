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

### 커밋 2: Member 엔티티 ~ 컨트롤러 구현 (GREEN)

요청이 처리되는 흐름: **컨트롤러 → 서비스 → 리포지터리 → DB**

**변경 파일 (모두 새 파일)**

| 파일 | 역할 |
|---|---|
| `member/entity/Member.java` | DB `member` 테이블과 연결되는 클래스. `username`(중복 불가), `password`, `name` |
| `member/repository/MemberRepository.java` | DB 접근. `findByUsername`은 메서드 이름만으로 스프링이 SQL을 만들어 줌 |
| `member/service/MemberService.java` | 업무 로직. `join`은 `Member`를 만들어 저장 |
| `member/controller/ApiV1MemberController.java` | HTTP 입구. `POST /api/v1/members/join` |

**핵심 포인트**

- `Member extends BaseEntity`: 번호(id), 생성일, 수정일은 068 코드의 `BaseEntity`가 이미 처리하므로 회원 고유 정보만 가집니다.
- `@Column(unique = true)`: DB 수준에서 username 중복을 막는 안전장치입니다.
- `MemberJoinReqBody`(record): 요청 JSON을 담는 그릇입니다. `@Size(min = 4, max = 30)` 같은 제약을 필드에 붙입니다.
- `@RequestBody @Valid`: 요청 JSON을 객체로 바꾸고 제약을 검사합니다. 어기면 기존 `GlobalExceptionHandler`가 `400-1`로 응답합니다.
- 상태 코드 `201`은 컨트롤러가 직접 정하지 않습니다. 068 코드의 `ResponseAspect`가 `RsData`의 `resultCode` 앞부분(`201`)을 읽어 HTTP 상태로 설정합니다.

**결과**: 회원가입 테스트 2개를 포함해 전체 테스트 통과(GREEN).

![커밋 2 테스트 결과](docs/images/02.png)

### 커밋 3: username 중복 시 409 테스트 추가 (RED)

**변경 파일**: `ApiV1MemberControllerTest.java`에 `t3` 추가

- 먼저 `memberService.join("dupUser", ...)`로 회원을 하나 만들어 둡니다(준비 단계).
- 같은 username `dupUser`로 가입 API를 호출합니다.
- 기대 결과: 상태 `409 Conflict`, `resultCode` `409-1`, 메시지 `dupUser(은)는 이미 사용중인 username 입니다.`

**결과**: 실패(RED). 아직 중복 검사 코드가 없어서 그대로 저장을 시도합니다. 그러다 DB의 `unique` 제약에 걸려 `DataIntegrityViolationException`이 나고, 409가 아닌 서버 오류가 됩니다.
→ DB 제약에만 맡기면 사용자에게 친절한 메시지를 줄 수 없습니다. 저장하기 **전에** 직접 검사해야 하는 이유입니다.

![커밋 3 테스트 결과](docs/images/03.png)

### 커밋 4: MemberDuplicateUsernameException으로 409 처리 (GREEN)

**변경 파일**

1. `global/globalExceptionHandler/GlobalExceptionHandler.java`
   - **예외 클래스 추가**: `MemberDuplicateUsernameException` (과제 명세대로 `GlobalExceptionHandler` 안에 내부 클래스로 둠)
     - 생성자에서 `"%s(은)는 이미 사용중인 username 입니다."` 메시지를 만들어 둡니다.
     - `IllegalArgumentException` 같은 일반 예외 대신 **이름만 봐도 무슨 상황인지 알 수 있는 전용 예외**를 씁니다.
   - **예외 처리기 추가**: `@ExceptionHandler(MemberDuplicateUsernameException.class)`
     - 앱 어디에서든 이 예외가 던져지면 이 메서드가 받아서 `409` + `{"resultCode": "409-1", "msg": ...}`로 바꿔 응답합니다.
   - import를 `HttpStatus.*`로 바꿔 `CONFLICT`도 쓸 수 있게 했습니다.
2. `member/service/MemberService.java`의 `join`
   ```java
   memberRepository.findByUsername(username)   // 같은 username 회원을 찾아본다
           .ifPresent(_ -> {                    // 있으면(= 중복이면)
               throw new MemberDuplicateUsernameException(username); // 예외를 던진다
           });
   ```
   - `Optional.ifPresent`는 "값이 있을 때만 실행"이라는 뜻입니다. `if (optional.isPresent())`를 짧게 쓴 형태입니다.
   - `_`는 Java 22+의 "사용하지 않는 변수" 표기입니다. 찾은 회원 객체는 쓰지 않으므로 `_`로 둡니다.

**흐름**: 컨트롤러 → `join` → 중복 발견 → 예외 → `GlobalExceptionHandler`가 409 응답

**결과**: 전체 테스트 통과(GREEN).

![커밋 4 테스트 결과](docs/images/04.png)

---

## 문제 2: 내 정보 API

`GET /api/v1/members/me?actorId={회원번호}`

### 커밋 5: 성공/실패 테스트 먼저 작성 (RED)

**변경 파일**: `ApiV1MemberControllerTest.java`에 `t4`, `t5`, `t6` 추가

- `t4` 내 정보 성공
  - 회원을 하나 가입시키고, 그 회원 번호를 `actorId`로 넘깁니다. `.param("actorId", ...)`는 주소 뒤에 `?actorId=...`를 붙입니다.
  - 응답 JSON에 `id`, `createDate`, `updateDate`, `username`, `name`이 있어야 합니다.
  - `jsonPath("$.password").doesNotExist()`: **비밀번호는 응답에 절대 나오면 안 됩니다.**
- `t5` 존재하지 않는 actorId(`999999`) → `401` / `401-1` / `로그인 후 이용해주세요.`
- `t6` actorId 자체가 없음 → 똑같이 `401`

**결과**: 3개 실패(RED). `/api/v1/members/me` 주소를 처리하는 메서드가 아직 없어서 404가 나옵니다.

![커밋 5 테스트 결과](docs/images/05.png)
