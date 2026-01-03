package com.example.backend_toyproject.model.dto;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.TodoCategoryMappingEntity;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.model.enums.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoDto {
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private Timestamp dueDate;
    private Priority priority;
    private TodoStatus status;
    private boolean completed;
    private Timestamp completedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;
    @Builder.Default
    private List<CategorySummaryDto> categories = new ArrayList<>();

    // Entity -> DTO 변환
    public TodoDto(TodoEntity todoEntity) {
        this.id = todoEntity.getId();
        this.userId = todoEntity.getUser().getId();
        this.title = todoEntity.getTitle();
        this.description = todoEntity.getDescription();
        this.dueDate = todoEntity.getDueDate();
        this.priority = todoEntity.getPriority();
        this.status = todoEntity.getStatus();
        this.completed = todoEntity.isCompleted();
        this.completedAt = todoEntity.getCompletedAt();
        this.createdAt = todoEntity.getCreatedAt();
        this.updatedAt = todoEntity.getUpdatedAt();
        this.deletedAt = todoEntity.getDeletedAt();

        this.categories = todoEntity.getCategoryLinks() == null
                ? new ArrayList<>()
                : todoEntity.getCategoryLinks().stream()
                .map(TodoCategoryMappingEntity::getCategory)
                .filter(Objects::nonNull)
                .map(CategorySummaryDto::CategorySummaryDto)
                .toList();
    }
}
