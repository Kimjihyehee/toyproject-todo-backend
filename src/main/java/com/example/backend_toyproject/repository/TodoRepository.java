package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.TodoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {
    // 할일 전체 조회 (단일 유저)
    Page<TodoEntity> findByUser_IdAndDeletedAtIsNullAndStartDateLessThanAndEndDateGreaterThan
    (UUID userId, Timestamp endTs, Timestamp startTs, Pageable pageable);

    // UUID user(UserEntity user);

    // 할일 단건 조회 (단일 유저)
    Optional<TodoEntity> findByIdAndDeletedAtIsNull(UUID todoId);
}
