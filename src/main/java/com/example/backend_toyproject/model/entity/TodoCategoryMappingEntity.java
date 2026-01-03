package com.example.backend_toyproject.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Entity
@Table(name = "todo_category_mapping", uniqueConstraints = {
        @UniqueConstraint(name = "uk_todo_category_pair", columnNames = {"todo_id", "category_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryMappingEntity {
    @EmbeddedId
    private TodoCategoryMappingId id;

    @MapsId("todoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    private TodoEntity todo;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}
