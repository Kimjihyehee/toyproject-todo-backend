package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByUser_IdAndNameIn(UUID userId, List<String> names);
}
