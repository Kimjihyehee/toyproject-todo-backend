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
import java.time.LocalDateTime;
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

    @Column(name = "start_date", nullable = false)
    private Timestamp startDate;

    @Column(name = "end_date", nullable = false)
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
        // startDate: 없으면 now
        Timestamp start = todoDto.getStartDate() != null
                ? todoDto.getStartDate()
                : Timestamp.valueOf(LocalDateTime.now());
        this.startDate = start;

        // endDate: 없으면 startDate 기준 "다음날 00:00" (exclusive)
        this.endDate = todoDto.getEndDate() != null
                ? todoDto.getEndDate()
                : Timestamp.valueOf(
                start.toLocalDateTime()
                        .toLocalDate()
                        .plusDays(1)
                        .atStartOfDay()
        );

        this.priority = todoDto.getPriority() != null ? todoDto.getPriority() : Priority.NORMAL;
        this.completed = todoDto.isCompleted();
        if(this.completed) {
            // 할일완료로 첫 입력한 경우
            this.status = TodoStatus.COMPLETED;
            this.completedAt = new Timestamp(System.currentTimeMillis());
        } else { // 할일 미완료로 첫 입력한 경우
            this.status = TodoStatus.CREATED;
            this.completedAt = null;
        }
    }

    // deletedAt값 입력 시 -> status를 DELETED로 동기화
    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
        if (deletedAt != null) {
            this.status = TodoStatus.DELETED;
        }
    }

    // completed 수정시 사용 - status, completedAt 동기화
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            // 할일 완료로 수정한 경우
            this.status = TodoStatus.COMPLETED;
            this.completedAt = new Timestamp(System.currentTimeMillis());
        } else {
            // 할일 미완료로 수정한 경우
            this.status = TodoStatus.UPDATED;
            this.completedAt = null;
        }
    }
}
