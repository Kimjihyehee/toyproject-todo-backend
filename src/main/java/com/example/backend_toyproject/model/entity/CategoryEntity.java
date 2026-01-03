package com.example.backend_toyproject.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_user_name", columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "uncategorized", nullable = false)
    private boolean uncategorized = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private Timestamp modifyDate;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<TodoCategoryMappingEntity> todoLinks = new ArrayList<>();

}
