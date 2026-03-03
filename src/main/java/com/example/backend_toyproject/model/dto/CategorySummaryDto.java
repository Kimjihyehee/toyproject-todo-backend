package com.example.backend_toyproject.model.dto;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategorySummaryDto {
    private UUID id;
    private String name;
    private UUID userId;
    private String description;
    private boolean uncategorized;

    // Entity -> dto 변환
    public CategorySummaryDto(CategoryEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.userId = entity.getUser().getId();
        this.description = entity.getDescription();
        this.uncategorized = entity.isUncategorized();
    }
}
