package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    /*
     * 1. 카테고리 생성
     */
    @PostMapping("/")
    public CategorySummaryDto createCategory(CategorySummaryDto dto) {
        return categoryService.createCategory(dto);
    }
}
