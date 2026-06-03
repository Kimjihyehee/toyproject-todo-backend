package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByUser_IdAndNameIn(UUID userId, List<String> names);

    // userId와 name 조합이 이미 존재하는지 확인
    boolean existsByNameAndUser_Id(String name, UUID userId);
}
