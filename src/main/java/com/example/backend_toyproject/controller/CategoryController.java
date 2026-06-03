package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    /*
     * 2. 카테고리 수정
     *  : 카테고리명만 수정할 수 있음
     */
    @PatchMapping("/update")
    public CategorySummaryDto updateCategory(@RequestBody CategorySummaryDto dto) {
        return categoryService.updateCategory(dto);
    }

    /*
     * 3. 카테고리 삭제
     */
    @DeleteMapping("/{categoryId}")
    public void deleteCategory(@PathVariable("categoryId") UUID categoryId) {
        categoryService.deleteCategory(categoryId);
    }

}
