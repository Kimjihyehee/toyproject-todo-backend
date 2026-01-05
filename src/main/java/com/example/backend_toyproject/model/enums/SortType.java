package com.example.backend_toyproject.model.enums;

import lombok.Getter;

@Getter
public enum SortType {
    CREATED_AT(SortDirection.DESC), // 최신 → 과거 (DESC)
    DUE_DATE(SortDirection.ASC),    // 마감 임박 → 늦은 순 (ASC)
    PRIORITY(SortDirection.ASC),    // 낮음 → 높음 (ASC)
    COMPLETED(SortDirection.ASC);   // 미완료 → 완료 (ASC)


    private final SortDirection defaultDirection;

    SortType(SortDirection defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

}
