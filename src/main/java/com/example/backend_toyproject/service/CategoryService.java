package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /*
     * 1. 카테고리 생성
     */
    public CategorySummaryDto createCategory(CategorySummaryDto dto) {
        // userId null 체크
        UUID userId = dto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to create a category");
        }
        // 사용자인지 체크
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        // dto -> entity 변환
        CategoryEntity categoryEntity = new CategoryEntity(dto);
        // user 정보 Entity에 연결 후 저장
        categoryEntity.setUser(user);
        // 카테고리 생성전, 해당 유저의 기존 카테고리에 동일한 이름의 카테고리가 있는 경우
        if(isDuplicateCategoryName(dto.getName(), userId)) {
            throw new IllegalArgumentException("Duplicate category names");
        }
        // 저장
        categoryEntity = categoryRepository.save(categoryEntity);
        // dto 형태로 return
        return new CategorySummaryDto(categoryEntity);
    }

    /*
     * 카테고리명 중복 체크
     */
    public boolean isDuplicateCategoryName(String name, UUID userId) {
        // 카테고리 테이블에 사용자 id를 기반으로 같은 카테고리명이 존재하는지 확인
      return categoryRepository.existsByCategoryNameAndUserId(name, userId);
    }

    /*
     * 2. 카테고리 수정
     *  : 카테고리명만 수정할 수 있음
     */
    public CategorySummaryDto updateCategory(CategorySummaryDto dto) {
        // 0. 유저 존재 확인
        userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));
        // 1. 수정하려는 카테고리명이 중복 존재 경우 체크
        if(isDuplicateCategoryName(dto.getName(), dto.getUserId())) {
            throw new IllegalArgumentException("Duplicate category names");
        };
        // 2. dto의 categoryId로 해당 row의 category_name 업데이트
        CategoryEntity category = categoryRepository.findByCategory_Id(dto.getId()).orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getId()));
        category.setName(dto.getName());
        categoryRepository.save(category);
        return new CategorySummaryDto(category);
    }

    /*
     * 3. 카테고리 삭제
     */
    public CategorySummaryDto deleteCategory(UUID id) {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        categoryEntity.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        categoryEntity = categoryRepository.save(categoryEntity);
        return new CategorySummaryDto(categoryEntity);
    }
}
