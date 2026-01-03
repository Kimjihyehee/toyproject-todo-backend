package com.example.backend_toyproject.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Embeddable
public class TodoCategoryMappingId implements Serializable {
    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoCategoryMappingId that = (TodoCategoryMappingId) o;
        return Objects.equals(todoId, that.todoId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(todoId, categoryId);
    }
}
