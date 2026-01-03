package com.example.backend_toyproject.model.dto;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
public class CategorySummaryDto {
    private final UUID id;
    private final String name;
    private final String description;
    private final boolean isDefault;

    // DTO -> Entity 변환
//    public CategorySummaryDto(CategoryEntity entity) {
//        this.id = entity.getId();
//        this.name = entity.getName();
//        this.description = entity.getDescription();
//        this.isDefault = entity.isDefault();
//    }

    public static CategorySummaryDto CategorySummaryDto(CategoryEntity entity) {
        return CategorySummaryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isDefault(entity.isDefault())
                .build();
    }
}
