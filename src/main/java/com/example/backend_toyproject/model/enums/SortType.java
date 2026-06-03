package com.example.backend_toyproject.model.enums;

import lombok.Getter;

// enum은 상수(ex. CREATED_AT)를 만들 때, 생성자를 호출
// 따라서, 생성자 SortType(SortDirection defaultDirection)에 SortDirection.DESC를 전달한다
@Getter
public enum SortType {
    CREATED_AT(SortDirection.DESC), // 최신 → 과거 (DESC)
    END_DATE(SortDirection.ASC),    // 할일 마감일 → 늦은 순 (ASC)
    START_DATE(SortDirection.ASC),    // 할일 시작일 → 늦은 순 (ASC)
    PRIORITY(SortDirection.ASC),    // 낮음 → 높음 (ASC)
    COMPLETED(SortDirection.ASC);   // 미완료 → 완료 (ASC)

    private final SortDirection defaultDirection;

    SortType(SortDirection defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

}
