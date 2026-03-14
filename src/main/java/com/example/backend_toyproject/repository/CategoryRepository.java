package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByUser_IdAndNameIn(UUID userId, List<String> names);

    // userId와 categoryName 조합이 이미 존재하지 않는지 확인
    boolean existsByCategoryNameAndUserId(String name, UUID userId);

    // 카테고리 id로 해당 row 조회
    Optional<CategoryEntity> findByCategory_Id(UUID id);
}
