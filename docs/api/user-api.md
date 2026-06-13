# User API 문서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api` |
| Prefix | `/user` |
| Content-Type | `application/json` |
| 삭제 방식 | Soft Delete (`deletedAt` 타임스탬프 설정) |

> Swagger UI: `http://localhost:8080/api/swagger-ui.html`  
> OpenAPI JSON: `http://localhost:8080/api/api-docs`

---

## 공통 스키마

### UserDto (요청/응답)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 사용자 ID (서버 생성, 응답 전용) |
| `name` | string | 사용자 이름 |
| `nickname` | string | 닉네임 (고유, 최대 50자) |
| `createdAt` | timestamp | 생성 시각 |
| `modifiedAt` | timestamp | 수정 시각 |
| `lastLoginAt` | timestamp | 마지막 로그인 시각 |
| `deletedAt` | timestamp | 삭제 시각 (null이면 활성 사용자) |
| `todos` | TodoDto[] | 연결된 할 일 목록 (조회 시 포함) |
| `categories` | CategorySummaryDto[] | 할 일에서 추출한 카테고리 목록 (조회 시 포함) |

**timestamp 형식:** `yyyy-MM-dd HH:mm:ss`  
(잘못된 형식 시 `400 Bad Request` — `"timestamp 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm:ss)"`)

### TodoDto (조회 응답에 포함)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 할 일 ID |
| `userId` | UUID | 소유 사용자 ID |
| `title` | string | 제목 |
| `description` | string | 설명 |
| `startDate` | timestamp | 시작일 |
| `endDate` | timestamp | 종료일 |
| `priority` | enum | `LOW`, `NORMAL`, `HIGH`, `URGENT` |
| `status` | enum | `CREATED`, `UPDATED`, `COMPLETED`, `DELETED`, `RESTORED` |
| `completed` | boolean | 완료 여부 |
| `completedAt` | timestamp | 완료 시각 |
| `createdAt` | timestamp | 생성 시각 |
| `updatedAt` | timestamp | 수정 시각 |
| `deletedAt` | timestamp | 삭제 시각 |
| `categories` | CategorySummaryDto[] | 연결된 카테고리 |

### CategorySummaryDto (조회 응답에 포함)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 카테고리 ID |
| `name` | string | 카테고리명 |
| `userId` | UUID | 소유 사용자 ID |

---

## 1. 유저 생성

활성 사용자(`deletedAt IS NULL`) 중 **nickname 중복**을 검사한 뒤 사용자를 생성합니다.

```
POST /api/user
```

### Request Body

| 필드 | 필수 | 설명 |
|------|------|------|
| `name` | O | 사용자 이름 |
| `nickname` | O | 닉네임 (고유) |

```json
{
  "name": "홍길동",
  "nickname": "hong123"
}
```

### Response `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "홍길동",
  "nickname": "hong123",
  "createdAt": "2026-06-13 10:00:00",
  "modifiedAt": "2026-06-13 10:00:00",
  "lastLoginAt": "2026-06-13 10:00:00",
  "deletedAt": null,
  "todos": [],
  "categories": []
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| nickname 중복 | `"nickname is duplicated"` |

---

## 2. 유저 정보 조회 (단일)

닉네임으로 **삭제되지 않은** 사용자를 조회합니다.  
응답에 해당 사용자의 **할 일 목록**과 **카테고리 목록**이 함께 포함됩니다.

```
GET /api/user/{nickName}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `nickName` | string | 조회할 사용자 닉네임 |

### Response `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "홍길동",
  "nickname": "hong123",
  "createdAt": "2026-06-13 10:00:00",
  "modifiedAt": "2026-06-13 10:00:00",
  "lastLoginAt": "2026-06-13 10:00:00",
  "deletedAt": null,
  "todos": [
    {
      "id": "...",
      "userId": "...",
      "title": "회의 준비",
      "description": "자료 정리",
      "startDate": "2026-06-13 09:00:00",
      "endDate": "2026-06-13 18:00:00",
      "priority": "HIGH",
      "status": "CREATED",
      "completed": false,
      "completedAt": null,
      "createdAt": "2026-06-13 10:00:00",
      "updatedAt": "2026-06-13 10:00:00",
      "deletedAt": null,
      "categories": [
        {
          "id": "...",
          "name": "업무",
          "userId": "..."
        }
      ]
    }
  ],
  "categories": [
    {
      "id": "...",
      "name": "업무",
      "userId": "..."
    }
  ]
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| 사용자 없음 | `"user is not exist"` |
| 삭제된 사용자 | `"user is not exist or deleted"` |

---

## 3. 유저 정보 수정

`name`, `nickname` 중 **null이 아닌 필드만** 부분 수정합니다.  
nickname 변경 시, 활성 사용자 중 동일 nickname이 이미 존재하면 예외가 발생합니다.

```
PATCH /api/user/{nickName}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `nickName` | string | 수정 대상 사용자 닉네임 (현재 닉네임) |

### Request Body

| 필드 | 필수 | 설명 |
|------|------|------|
| `name` | X | 변경할 이름 |
| `nickname` | X | 변경할 닉네임 |

> 둘 다 생략(null) 가능. 보낸 필드만 수정됩니다.

```json
{
  "name": "김철수",
  "nickname": "kim456"
}
```

### Response `200 OK`

수정된 사용자 정보 (`UserDto`)

### 에러

| 조건 | 메시지 |
|------|--------|
| 유저 없음 또는 삭제됨 | `"nickName을 조회한 결과, 탈퇴한 회원입니다."` |
| name이 기존과 동일 | `"기존의 name과 동일한 name을 입력했습니다."` |
| nickname이 기존과 동일 | `"기존의 nickName과 동일한 nickName을 입력했습니다."` |
| nickname 중복 (다른 유저 사용 중) | `"이미 사용중인 닉네임이 존재합니다."` |

---

## 4. 유저 삭제 (Soft Delete)

사용자의 `deletedAt`에 현재 시각을 설정합니다. DB에서 물리 삭제하지 않습니다.

```
DELETE /api/user/{nickName}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `nickName` | string | 삭제할 사용자 닉네임 |

### Response `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "홍길동",
  "nickname": "hong123",
  "createdAt": "2026-06-13 10:00:00",
  "modifiedAt": "2026-06-13 10:00:00",
  "lastLoginAt": "2026-06-13 10:00:00",
  "deletedAt": "2026-06-13 12:00:00",
  "todos": [],
  "categories": []
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| 사용자 없음 | `"존재하지 않는 사용자 입니다."` |
| 이미 삭제됨 | `"이미 삭제된 사용자 입니다."` |

---

## API 요약

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | `POST` | `/api/user` | 유저 생성 |
| 2 | `GET` | `/api/user/{nickName}` | 유저 + 할 일 + 카테고리 조회 |
| 3 | `PATCH` | `/api/user/{nickName}` | 유저 정보 수정 |
| 4 | `DELETE` | `/api/user/{nickName}` | 유저 Soft Delete |

---

## 비즈니스 규칙

1. **nickname 고유성:** 생성·수정 모두 `deletedAt IS NULL`인 활성 사용자 기준으로 nickname 중복 불가
2. **Soft Delete:** 삭제된 사용자는 조회·수정 대상에서 제외
3. **부분 수정:** PATCH는 null 필드는 무시, 값이 있는 필드만 변경
4. **동일값 수정 방지:** name/nickname을 기존과 같은 값으로 바꾸려 하면 예외 발생

---

## 참고 사항

- `IllegalArgumentException`에 대한 전역 핸들러가 없어, 위 에러들은 현재 **500 Internal Server Error**로 반환될 수 있습니다. (`GlobalExceptionHandler`는 timestamp 파싱 오류만 처리)
