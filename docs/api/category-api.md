# Category API 문서

## 기본 정보

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080/api` |
| Prefix | `/category` |
| 삭제 방식 | Soft Delete (`deletedAt` 타임스탬프 설정) |

> Swagger UI: `http://localhost:8080/api/swagger-ui.html`  
> OpenAPI JSON: `http://localhost:8080/api/api-docs`

---

## 공통 스키마

### CategorySummaryDto (요청/응답)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 카테고리 ID (서버 생성, 수정/삭제 시 필요) |
| `name` | string | 카테고리명 (최대 50자, 유저별 고유) |
| `userId` | UUID | 소유 사용자 ID |

---

## 1. 카테고리 생성

동일 사용자(`userId`) 내 **카테고리명 중복**을 검사한 뒤 카테고리를 생성합니다.

```
POST /api/category/
```

### Request (Query Parameter)

> `@RequestBody`가 아닌 **Query Parameter**로 전달합니다.

| 이름 | 필수 | 설명 |
|------|------|------|
| `userId` | O | 소유 사용자 ID |
| `name` | O | 카테고리명 |

### 요청 예시

```
POST /api/category/?userId=550e8400-e29b-41d4-a716-446655440000&name=업무
```

### Response `200 OK`

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "업무",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| userId 누락 | `"userId is required to create a category"` |
| 사용자 없음 | `"User not found: {userId}"` |
| 카테고리명 중복 | `"Duplicate category names"` |

---

## 2. 카테고리 수정

**카테고리명(`name`)만** 수정할 수 있습니다.

```
PATCH /api/category/update
```

### Request Body

| 필드 | 필수 | 설명 |
|------|------|------|
| `id` | O | 수정할 카테고리 ID |
| `userId` | O | 소유 사용자 ID |
| `name` | O | 변경할 카테고리명 |

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "개인"
}
```

### Response `200 OK`

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "개인",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 에러

| 조건 | 메시지 |
|------|--------|
| 사용자 없음 | `"User not found: {userId}"` |
| 카테고리명 중복 | `"Duplicate category names"` |
| 카테고리 없음 | `"Category not found: {id}"` |

---

## 3. 카테고리 삭제 (Soft Delete)

카테고리의 `deletedAt`에 현재 시각을 설정합니다.

```
DELETE /api/category/{categoryId}
```

### Path Parameter

| 이름 | 타입 | 설명 |
|------|------|------|
| `categoryId` | UUID | 삭제할 카테고리 ID |

### Response `200 OK`

응답 본문 없음 (void)

### 에러

| 조건 | 메시지 |
|------|--------|
| 카테고리 없음 | `"Category not found: {categoryId}"` |

---

## API 요약

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | `POST` | `/api/category/` | 카테고리 생성 (Query Parameter) |
| 2 | `PATCH` | `/api/category/update` | 카테고리명 수정 |
| 3 | `DELETE` | `/api/category/{categoryId}` | 카테고리 Soft Delete |

---

## 비즈니스 규칙

1. **카테고리명 고유성:** 동일 `userId` 내에서 카테고리명 중복 불가 (DB unique constraint: `user_id + name`)
2. **Soft Delete:** 삭제 시 `deletedAt` 설정, 물리 삭제하지 않음
3. **유저 소유:** 카테고리는 반드시 특정 사용자에 귀속
4. **Todo 연동:** Todo 수정 시 `categories` 필드에 카테고리명을 전달하여 연결/재연결

---

## 참고 사항

- 카테고리 **생성** API는 JSON Body가 아닌 **Query Parameter** 방식입니다.
- 카테고리 **삭제** API는 서비스에서 `CategorySummaryDto`를 반환하지만, 컨트롤러는 `void`로 응답 본문을 반환하지 않습니다.
- `IllegalArgumentException`은 전역 핸들러가 없어 **500 Internal Server Error**로 반환될 수 있습니다.
- 수정 시 중복 검사는 **자기 자신의 기존 이름**도 중복으로 판단할 수 있습니다. (동일 이름으로 수정 시 `"Duplicate category names"` 발생 가능)
