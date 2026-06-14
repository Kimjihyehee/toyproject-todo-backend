# Todo API 문서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api` |
| Prefix | `/todo` |
| Content-Type | `application/json` |
| 삭제 방식 | Soft Delete (`deletedAt` 타임스탬프 설정) |

> Swagger UI: `http://localhost:8080/api/swagger-ui.html`  
> OpenAPI JSON: `http://localhost:8080/api/api-docs`

---

## 공통 스키마

### TodoDto (생성 요청 / 응답)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 할 일 ID (서버 생성, 응답 전용) |
| `userId` | UUID | 소유 사용자 ID |
| `title` | string | 제목 |
| `description` | string | 설명 (최대 1000자) |
| `startDate` | timestamp | 시작일 |
| `endDate` | timestamp | 종료일 |
| `priority` | enum | `LOW`, `NORMAL`, `HIGH`, `URGENT` (기본값: `NORMAL`) |
| `status` | enum | `CREATED`, `UPDATED`, `COMPLETED`, `DELETED`, `RESTORED` |
| `completed` | boolean | 완료 여부 (기본값: `false`) |
| `completedAt` | timestamp | 완료 시각 |
| `createdAt` | timestamp | 생성 시각 |
| `updatedAt` | timestamp | 수정 시각 |
| `deletedAt` | timestamp | 삭제 시각 |
| `categories` | CategorySummaryDto[] | 연결된 카테고리 목록 |

**timestamp 형식:** `yyyy-MM-dd HH:mm:ss`

### TodoUpdateRequestDTO (수정 요청)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `todoId` | UUID | O | 수정할 할 일 ID |
| `userId` | UUID | O | 소유 사용자 ID |
| `title` | string | X | 제목 |
| `description` | string | X | 설명 |
| `startDate` | timestamp | X | 시작일 (null이면 기존값 유지) |
| `endDate` | timestamp | X | 종료일 (null이면 기존값 유지) |
| `priority` | enum | X | 우선순위 |
| `completed` | boolean | X | 완료 여부 |
| `categories` | string[] | X | 카테고리명 목록 (null이면 변경 없음) |

### CategorySummaryDto (응답에 포함)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 카테고리 ID |
| `name` | string | 카테고리명 |
| `userId` | UUID | 소유 사용자 ID |

### 조회 Query Parameter Enum

**TodoViewType**

| 값 | 설명 |
|----|------|
| `MONTH` | 월/일 단위 조회 (기본값) |
| `DAY` | 일 단위 조회 |

**SortType** (기본값: `CREATED_AT`)

| 값 | 기본 정렬 방향 | 설명 |
|----|---------------|------|
| `CREATED_AT` | `DESC` | 생성일순 |
| `START_DATE` | `ASC` | 시작일순 |
| `END_DATE` | `ASC` | 마감일순 |
| `PRIORITY` | `ASC` | 우선순위순 (낮음 → 높음) |
| `COMPLETED` | `ASC` | 완료/미완료순 |

**SortDirection**

| 값 | 설명 |
|----|------|
| `ASC` | 오름차순 |
| `DESC` | 내림차순 |

> `sortDirection`을 생략하면 `sortType`의 기본 정렬 방향이 적용됩니다.

---

## 1. 할 일 생성

```
POST /api/todo
```

### Request Body

| 필드 | 필수 | 설명 |
|------|------|------|
| `userId` | O | 소유 사용자 ID |
| `title` | O | 제목 |
| `description` | X | 설명 |
| `startDate` | X | 시작일 (미입력 시 현재 시각) |
| `endDate` | X | 종료일 (미입력 시 startDate 기준 다음날 00:00) |
| `priority` | X | 우선순위 (미입력 시 `NORMAL`) |
| `completed` | X | 완료 여부 (미입력 시 `false`) |

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "회의 준비",
  "description": "발표 자료 정리",
  "startDate": "2026-06-13 09:00:00",
  "endDate": "2026-06-13 18:00:00",
  "priority": "HIGH",
  "completed": false
}
```

### Response `200 OK`

생성된 `TodoDto`

```json
{
  "id": "...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "회의 준비",
  "description": "발표 자료 정리",
  "startDate": "2026-06-13 09:00:00",
  "endDate": "2026-06-13 18:00:00",
  "priority": "HIGH",
  "status": "CREATED",
  "completed": false,
  "completedAt": null,
  "createdAt": "2026-06-13 10:00:00",
  "updatedAt": "2026-06-13 10:00:00",
  "deletedAt": null,
  "categories": []
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| userId 누락 | `"userId is required to create a todo"` |
| 사용자 없음 | `"User not found: {userId}"` |

---

## 2. 할 일 목록 조회 (단일 유저)

할 일의 `startDate`~`endDate` 기간이 조회 기간과 **겹치는** 항목을 페이징 조회합니다.  
삭제되지 않은(`deletedAt IS NULL`) 할 일만 대상입니다.

```
GET /api/todo/{userId}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `userId` | UUID | 조회할 사용자 ID |

### Query Parameter

| 이름 | 필수 | 기본값 | 설명 |
|------|------|--------|------|
| `viewType` | O | `MONTH` | 조회 뷰 타입 (`MONTH`, `DAY`) |
| `year` | O | - | 조회 연도 |
| `month` | X | - | 조회 월 (1~12) |
| `day` | X | - | 조회 일 (1~31, `month`와 함께 사용) |
| `sortType` | X | `CREATED_AT` | 정렬 기준 |
| `sortDirection` | X | sortType 기본값 | 정렬 방향 |
| `page` | X | `0` | 페이지 번호 (0 이상) |
| `size` | X | `10` | 페이지 크기 (1 이상) |

### 조회 기간 규칙

| year | month | day | 조회 범위 |
|------|-------|-----|----------|
| O | X | X | 해당 연도 전체 |
| O | O | X | 해당 월 전체 |
| O | O | O | 해당 일 전체 |

### 요청 예시

```
GET /api/todo/{userId}?viewType=MONTH&year=2026&month=6&sortType=END_DATE&sortDirection=ASC&page=0&size=10
```

### Response `200 OK`

`TodoDto[]`

```json
[
  {
    "id": "...",
    "userId": "...",
    "title": "회의 준비",
    "description": "발표 자료 정리",
    "startDate": "2026-06-13 09:00:00",
    "endDate": "2026-06-13 18:00:00",
    "priority": "HIGH",
    "status": "CREATED",
    "completed": false,
    "completedAt": null,
    "createdAt": "2026-06-13 10:00:00",
    "updatedAt": "2026-06-13 10:00:00",
    "deletedAt": null,
    "categories": []
  }
]
```

### 에러

| HTTP | 조건 | 메시지 |
|------|------|--------|
| 400 | year 누락 | `"year는 필수입니다."` |
| 400 | page < 0 | `"page는 0 이상이어야 합니다."` |
| 400 | size <= 0 | `"size는 1 이상이어야 합니다."` |
| 400 | MONTH + day만 있음 | `"월이 없는 일 조회는 불가합니다."` |
| 400 | month 범위 오류 | `"month는 1~12 사이여야 합니다."` |
| 400 | day 범위 오류 | `"day는 1~31 사이여야 합니다."` |
| 400 | day만 있고 month 없음 | `"day는 month와 함께 전달되어야 합니다."` |
| 400 | 잘못된 날짜 | `"유효하지 않은 날짜입니다: {year}-{month}-{day}"` |
| 500 | 사용자 없음 | `"User not found: {userId}"` |

---

## 3. 할 일 수정

`title`, `description`, `startDate`, `endDate`, `priority`, `completed`, `categories` 중 **null이 아닌 필드만** 수정합니다.

```
PATCH /api/todo
```

### Request Body

```json
{
  "todoId": "...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "회의 준비 (수정)",
  "description": "자료 + 리허설",
  "startDate": "2026-06-13 10:00:00",
  "endDate": "2026-06-13 19:00:00",
  "priority": "URGENT",
  "completed": true,
  "categories": ["업무", "개인"]
}
```

### Response `200 OK`

수정된 `TodoDto`

### 에러

| HTTP | 조건 | 메시지 |
|------|------|--------|
| 400 | startDate >= endDate | `"startDate는 endDate보다 이전이어야 합니다."` |
| 400 | todoId/userId 누락 | Bean Validation 오류 |
| 500 | 사용자 없음 | `"User not found: {userId}"` |
| 500 | 할 일 없음 | `"Todo not found:"` |
| 500 | 카테고리명 중복 | `"Duplicate category names"` |
| 500 | 존재하지 않는 카테고리 | `"Some categories not found"` |

### 수정 시 상태 변경 규칙

- 일반 필드 수정 시: `status` → `UPDATED`
- `completed = true` 수정 시: `status` → `COMPLETED`, `completedAt` 설정
- `completed = false` 수정 시: `status` → `UPDATED`, `completedAt` → `null`
- `categories` 수정 시: 기존 매핑 전체 삭제 후 재생성 (해당 유저 소유 카테고리만 연결)

---

## 4. 할 일 단건 조회

```
GET /api/todo/{userId}/{todoId}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `userId` | UUID | 소유 사용자 ID |
| `todoId` | UUID | 할 일 ID |

### Response `200 OK`

`TodoDto`

### 에러

| 조건 | 메시지 |
|------|--------|
| 사용자 없음 | `"User not found: {userId}"` |
| 할 일 없음 | `"Todo not found: {todoId}"` |

---

## 5. 할 일 삭제 (Soft Delete)

```
DELETE /api/todo/{userId}/{todoId}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `userId` | UUID | 소유 사용자 ID |
| `todoId` | UUID | 할 일 ID |

### Response `200 OK`

삭제 처리된 `TodoDto` (`deletedAt` 설정, `status` → `DELETED`)

### 에러

| 조건 | 메시지 |
|------|--------|
| 사용자 없음 | `"User not found: {userId}"` |
| 할 일 없음 | `"Todo not found: {todoId}"` |

---

## API 요약

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | `POST` | `/api/todo` | 할 일 생성 |
| 2 | `GET` | `/api/todo/{userId}` | 할 일 목록 조회 (기간 필터 + 페이징) |
| 3 | `PATCH` | `/api/todo` | 할 일 수정 |
| 4 | `GET` | `/api/todo/{userId}/{todoId}` | 할 일 단건 조회 |
| 5 | `DELETE` | `/api/todo/{userId}/{todoId}` | 할 일 Soft Delete |

---

## 비즈니스 규칙

1. **기간 겹침 조회:** `[todo.startDate < queryEnd] AND [todo.endDate > queryStart]` 조건으로 필터링
2. **Soft Delete:** 삭제된 할 일은 조회·수정 대상에서 제외
3. **생성 기본값:** `startDate`/`endDate`/`priority` 미입력 시 서버 기본값 적용
4. **수정 시 날짜 검증:** `startDate`는 항상 `endDate`보다 이전이어야 함
5. **카테고리 연결:** 수정 시 카테고리명은 해당 유저가 소유한 카테고리만 허용

---

## 참고 사항

- `IllegalArgumentException`은 전역 핸들러가 없어 **500 Internal Server Error**로 반환될 수 있습니다.
- `ResponseStatusException`은 **400 Bad Request**로 반환됩니다.
- 할 일 생성 API는 `@Valid` 검증이 없어, 필수 필드 누락 시 서비스 레이어에서 예외가 발생합니다.
