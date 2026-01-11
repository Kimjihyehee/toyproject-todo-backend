package com.example.backend_toyproject.model.entity;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.model.enums.TodoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "todo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_date")
    private Timestamp startDate;

    @Column(name = "end_date")
    private Timestamp endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private TodoStatus status = TodoStatus.CREATED;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private Timestamp completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoCategoryMappingEntity> categoryLinks = new ArrayList<>();

    // DTO -> Entity 변환
    public TodoEntity(TodoDto todoDto) {
        this.id = todoDto.getId();
        this.title = todoDto.getTitle() != null ? todoDto.getTitle() : "";
        this.description = todoDto.getDescription();
        this.startDate = todoDto.getStartDate();
        this.endDate = todoDto.getEndDate();
        this.priority = todoDto.getPriority() != null ? todoDto.getPriority() : Priority.NORMAL;
    }
}

