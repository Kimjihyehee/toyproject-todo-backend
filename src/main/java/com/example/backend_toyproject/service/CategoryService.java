package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public CategorySummaryDto createCategory(CategorySummaryDto dto) {
        // dto -> entity 변환 -> save
        CategoryEntity categoryEntity = categoryRepository.save(new CategoryEntity(dto));
        // dto 형태로 return
        return new CategorySummaryDto(categoryEntity);
    }

}
