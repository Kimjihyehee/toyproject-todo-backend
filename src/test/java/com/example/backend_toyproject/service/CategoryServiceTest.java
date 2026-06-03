package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("createCategory - 유효한 userId로 카테고리 생성 성공")
    void testCreateCategory_Success() {
        // given
        UserEntity user = saveFakeUser();
        CategorySummaryDto request = new CategorySummaryDto();
        request.setUserId(user.getId());
        request.setName("work");

        // when
        CategorySummaryDto result = categoryService.createCategory(request);

        // then (반환 DTO)
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("work");

        // then (DB)
        List<CategoryEntity> all = categoryRepository.findAll();
        assertThat(all).hasSize(1);
        CategoryEntity saved = all.get(0);
        assertThat(saved.getName()).isEqualTo("work");
        assertThat(saved.getUser()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("createCategory - userId가 null이면 IllegalArgumentException 발생")
    void testCreateCategory_UserIdNull_Throws() {
        // given
        CategorySummaryDto request = new CategorySummaryDto();
        request.setName("work");

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId is required");
    }

    @Test
    @DisplayName("createCategory - 존재하지 않는 userId면 IllegalArgumentException 발생")
    void testCreateCategory_UserNotFound_Throws() {
        // given
        CategorySummaryDto request = new CategorySummaryDto();
        request.setUserId(UUID.randomUUID());
        request.setName("work");

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("createCategory - 동일 유저의 중복 카테고리명이면 IllegalArgumentException 발생")
    void testCreateCategory_DuplicateName_Throws() {
        // given
        UserEntity user = saveFakeUser();
        saveFakeCategory(user, "work");
        CategorySummaryDto request = new CategorySummaryDto();
        request.setUserId(user.getId());
        request.setName("work");

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate category names");
    }

    @Test
    @DisplayName("updateCategory - 카테고리명 수정 성공")
    void testUpdateCategory_Success() {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity category = saveFakeCategory(user, "work");
        CategorySummaryDto dto = new CategorySummaryDto();
        dto.setId(category.getId());
        dto.setUserId(user.getId());
        dto.setName("personal");

        // when
        CategorySummaryDto result = categoryService.updateCategory(dto);

        // then (반환 DTO)
        assertThat(result.getId()).isEqualTo(category.getId());
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("personal");

        // then (DB)
        CategoryEntity updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("personal");
    }

    @Test
    @DisplayName("updateCategory - 존재하지 않는 카테고리 id면 IllegalArgumentException 발생")
    void testUpdateCategory_CategoryNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();
        CategorySummaryDto dto = new CategorySummaryDto();
        dto.setId(UUID.randomUUID());
        dto.setUserId(user.getId());
        dto.setName("personal");

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("updateCategory - 존재하지 않는 userId면 IllegalArgumentException 발생")
    void testUpdateCategory_UserNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity category = saveFakeCategory(user, "work");
        CategorySummaryDto dto = new CategorySummaryDto();
        dto.setId(category.getId());
        dto.setUserId(UUID.randomUUID());
        dto.setName("personal");

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("updateCategory - 이미 존재하는 카테고리명으로 수정 시 IllegalArgumentException 발생")
    void testUpdateCategory_DuplicateName_Throws() {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity work = saveFakeCategory(user, "work");
        saveFakeCategory(user, "home");
        CategorySummaryDto dto = new CategorySummaryDto();
        dto.setId(work.getId());
        dto.setUserId(user.getId());
        dto.setName("home");

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate category names");
    }

    @Test
    @DisplayName("deleteCategory - soft delete 성공, deletedAt 세팅 및 DTO 반환")
    void testDeleteCategory_Success() {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity category = saveFakeCategory(user, "work");

        // when
        CategorySummaryDto result = categoryService.deleteCategory(category.getId());
        categoryRepository.flush();

        // then (반환 DTO)
        assertThat(result.getId()).isEqualTo(category.getId());
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("work");

        // then (DB)
        CategoryEntity deleted = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteCategory - 존재하지 않는 카테고리 id면 IllegalArgumentException 발생")
    void testDeleteCategory_CategoryNotFound_Throws() {
        // given
        UUID missingCategoryId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(missingCategoryId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");
    }

    private UserEntity saveFakeUser() {
        UserEntity user = new UserEntity();
        user.setName("test-user");
        user.setNickname("test-" + UUID.randomUUID());
        return userRepository.saveAndFlush(user);
    }

    private CategoryEntity saveFakeCategory(UserEntity user, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setUser(user);
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }
}

