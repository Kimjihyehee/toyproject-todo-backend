package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.TodoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {
    Page<TodoEntity> findByUser_IdAndStartDateLessThanAndEndDateGreaterThan(UUID userId, Timestamp endTs, Timestamp startTs, Pageable pageable);

    /*
     * 2. 할일 전체 조회(단일 유저)
     */
//    Page<TodoEntity> findByUser_Id(UUID userId, Pageable pageable);
}
