package com.example.backend_toyproject.exception;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // ========== 성공 코드 (S로 시작) ==========
    SUCCESS("S000", "요청이 성공적으로 처리되었습니다"),
    VALIDATION_SUCCESS("S001", "모든 데이터가 유효합니다"),
    AVAILABLE_NAME("S002", "사용 가능한 이름입니다"),

    // ========== 공통 에러 코드 (E로 시작) ==========
    // 400 Bad Request 관련
    INVALID_JSON("E001", "JSON 형식이 올바르지 않습니다"),
    EMPTY_REQUEST_BODY("E002", "요청 데이터가 없습니다"),
    MISSING_REQUIRED_FIELDS("E003", "필수 필드가 누락되었습니다"),
    VALIDATION_ERROR("E004", "요청 데이터가 올바르지 않습니다"),
    INVALID_REQUEST_PARAMETER("E005", "요청 파라미터가 올바르지 않습니다"),
    INVALID_PATH_PARAMETER("E006", "유효하지 않은 경로 파라미터입니다"),
    ID_MISMATCH("E007", "경로의 ID와 본문 ID가 일치하지 않습니다"),
    INVALID_ID_FORMAT("E008", "ID 형식이 올바르지 않습니다"),

    // 401 Unauthorized
    UNAUTHORIZED("E100", "인증이 필요합니다"),
    INVALID_TOKEN("E101", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED("E102", "토큰이 만료되었습니다"),

    // 403 Forbidden
    PERMISSION_DENIED("E200", "권한이 없습니다"),

    // 404 Not Found
    RESOURCE_NOT_FOUND("E300", "리소스를 찾을 수 없습니다"),

    // 409 Conflict
    DUPLICATE_RESOURCE("E400", "중복된 리소스입니다"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("E500", "서버 내부 오류가 발생했습니다"),

    // ========== TODO 관련 에러 코드 (T로 시작) ==========
    // 필드 유효성 검사
    TODO_TITLE_REQUIRED("T001", "제목은 필수 항목입니다"),
    TODO_TITLE_LENGTH_EXCEEDED("T002", "제목은 1-255자 이내여야 합니다"),
    TODO_DESCRIPTION_LENGTH_EXCEEDED("T003", "설명은 최대 1000자까지 입력 가능합니다"),
    TODO_INVALID_DATE_FORMAT("T004", "날짜는 YYYY-MM-DD 형식이어야 합니다"),
    TODO_INVALID_PRIORITY("T005", "우선순위는 'urgent', 'high', 'normal', 'low' 중 하나여야 합니다"),
    TODO_INVALID_STATUS("T006", "상태값이 올바르지 않습니다"),
    TODO_PAST_DUE_DATE("T007", "마감일은 현재 날짜 이후여야 합니다"),
    TODO_CATEGORY_REQUIRED("T008", "카테고리는 필수 항목입니다"),

    // TODO 비즈니스 로직
    TODO_NOT_FOUND("T100", "할일을 찾을 수 없습니다"),
    TODO_ALREADY_COMPLETED("T101", "이미 완료된 할일입니다"),
    TODO_ALREADY_DELETED("T102", "이미 삭제된 할일입니다"),
    TODO_DELETED_RESOURCE("T103", "삭제된 할일은 상태를 변경할 수 없습니다"),
    TODO_NOT_IN_TRASH("T104", "휴지통에 없는 할일입니다"),
    TODO_RESTORE_FAILED("T105", "할일 복구에 실패했습니다"),
    TODO_CREATE_SUCCESS("T200", "할일이 성공적으로 생성되었습니다"),
    TODO_UPDATE_SUCCESS("T201", "할일이 성공적으로 수정되었습니다"),
    TODO_DELETE_SUCCESS("T202", "할일이 휴지통으로 이동되었습니다"),
    TODO_COMPLETE_SUCCESS("T203", "할일이 완료되었습니다"),
    TODO_INCOMPLETE_SUCCESS("T204", "할일이 미완료 상태로 변경되었습니다"),
    TODO_RESTORE_SUCCESS("T205", "할일이 성공적으로 복구되었습니다"),

    // ========== 카테고리 관련 에러 코드 (C로 시작) ==========
    // 필드 유효성 검사
    CATEGORY_NAME_REQUIRED("C001", "카테고리명은 필수 항목입니다"),
    CATEGORY_NAME_LENGTH_EXCEEDED("C002", "카테고리명은 최대 50자까지 입력 가능합니다"),
    CATEGORY_DESCRIPTION_LENGTH_EXCEEDED("C003", "설명은 최대 255자까지 입력 가능합니다"),
    CATEGORY_INVALID_CHARACTERS("C004", "카테고리명에는 특수문자를 사용할 수 없습니다"),

    // 카테고리 비즈니스 로직
    CATEGORY_NOT_FOUND("C100", "존재하지 않는 카테고리입니다"),
    CATEGORY_NAME_DUPLICATE("C101", "이미 존재하는 카테고리명입니다"),
    CATEGORY_DEFAULT_DELETE("C102", "기본 카테고리는 삭제할 수 없습니다"),
    CATEGORY_HAS_TODOS("C103", "연결된 할일이 있어 대체 카테고리가 필요합니다"),
    CATEGORY_MISSING_REPLACEMENT("C104", "대체 카테고리 ID가 필요합니다"),
    CATEGORY_INVALID_REPLACEMENT("C105", "유효하지 않은 대체 카테고리입니다"),
    CATEGORY_CREATE_SUCCESS("C200", "카테고리가 성공적으로 생성되었습니다"),
    CATEGORY_UPDATE_SUCCESS("C201", "카테고리가 성공적으로 수정되었습니다"),
    CATEGORY_DELETE_SUCCESS("C202", "카테고리가 성공적으로 삭제되었습니다"),
    CATEGORY_AVAILABLE("C203", "사용 가능한 카테고리명입니다"),

    // ========== 페이지네이션/정렬 관련 에러 코드 (P로 시작) ==========
    INVALID_PAGE_NUMBER("P001", "페이지 번호는 0 이상의 정수여야 합니다"),
    INVALID_PAGE_SIZE("P002", "페이지 크기는 1 이상 100 이하의 정수여야 합니다"),
    INVALID_SORT_FIELD("P003", "유효하지 않은 정렬 필드입니다"),
    INVALID_SORT_DIRECTION("P004", "정렬 방향은 'asc' 또는 'desc'여야 합니다"),
    INVALID_SORT_PARAMS("P005", "정렬 파라미터가 올바르지 않습니다"),

    // ========== 필터링 관련 에러 코드 (F로 시작) ==========
    INVALID_FILTER_VALUE("F001", "유효하지 않은 필터 값입니다"),
    INVALID_DATE_RANGE("F002", "시작일이 종료일보다 늦을 수 없습니다"),
    INVALID_BOOLEAN_VALUE("F003", "true 또는 false 값이어야 합니다"),

    // ========== 유효성 검사 세부 코드 (V로 시작) ==========
    REQUIRED_FIELD_MISSING("V001", "필수 항목입니다"),
    LENGTH_EXCEEDED("V002", "최대 길이를 초과했습니다"),
    INVALID_VALUE("V003", "유효하지 않은 값입니다"),
    INVALID_FORMAT("V004", "형식이 올바르지 않습니다"),
    ;

    private final String code;
    private final String description;

    ResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
