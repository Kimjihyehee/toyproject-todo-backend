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
        request.setDescription("Work related tasks");
        request.setUncategorized(false);

        // when
        CategorySummaryDto result = categoryService.createCategory(request);

        // then (반환 DTO)
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("work");
        assertThat(result.getDescription()).isEqualTo("Work related tasks");
        assertThat(result.isUncategorized()).isFalse();

        // then (DB)
        List<CategoryEntity> all = categoryRepository.findAll();
        assertThat(all).hasSize(1);
        CategoryEntity saved = all.get(0);
        assertThat(saved.getName()).isEqualTo("work");
        assertThat(saved.getDescription()).isEqualTo("Work related tasks");
        assertThat(saved.isUncategorized()).isFalse();
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

    private UserEntity saveFakeUser() {
        UserEntity user = new UserEntity();
        user.setName("test-user");
        user.setNickname("test-" + UUID.randomUUID());
        return userRepository.saveAndFlush(user);
    }
}

