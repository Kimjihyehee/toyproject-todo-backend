package com.example.backend_toyproject.model.dto;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.TodoCategoryMappingEntity;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String nickname;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private Timestamp lastLoginAt;
    private Timestamp deletedAt;
    private List<TodoDto> todos;
    private List<CategorySummaryDto> categories;

    // Entity -> DTO 변환
    public UserDto(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.name = userEntity.getName();
        this.nickname = userEntity.getNickname();
        this.createdAt = userEntity.getCreatedAt();
        this.modifiedAt = userEntity.getModifiedAt();
        this.lastLoginAt = userEntity.getLastLoginAt();
        this.deletedAt = userEntity.getDeletedAt();
        this.todos = userEntity.getTodos().stream()
                .map(TodoDto::new)
                .toList();
        this.categories = userEntity.getTodos().stream()
                .flatMap(todo -> todo.getCategoryLinks().stream())
                .map(TodoCategoryMappingEntity::getCategory)
                .filter(Objects::nonNull)
                .map(CategorySummaryDto::new)
                .distinct()
                .toList();
    }
}
