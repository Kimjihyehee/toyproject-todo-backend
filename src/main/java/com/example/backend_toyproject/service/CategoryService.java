package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

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
        categoryEntity = categoryRepository.save(categoryEntity);
        // dto 형태로 return
        return new CategorySummaryDto(categoryEntity);
    }

}
