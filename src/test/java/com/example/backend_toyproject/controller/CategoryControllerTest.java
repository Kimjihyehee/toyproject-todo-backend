package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.CategorySummaryDto;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("createCategory - 유저 ID와 함께 카테고리 생성 성공")
    void testCreateCategory_Success() throws Exception {
        // given
        UserEntity user = saveFakeUser();

        String name = "work";

        // when & then (HTTP 응답 검증)
        mockMvc.perform(post("/category/")
                        .param("userId", user.getId().toString())
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value(name));

        // then (DB 저장 검증)
        List<CategoryEntity> all = categoryRepository.findAll();
        assertThat(all).hasSize(1);

        CategoryEntity saved = all.get(0);
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getUser()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("createCategory - userId 없이 호출 시 IllegalArgumentException 발생")
    void testCreateCategory_UserIdMissing_Throws() {
        // given
        String name = "work";

        // when & then (예외 및 메시지 검증)
        assertThatThrownBy(() ->
                mockMvc.perform(post("/category/")
                                .param("name", name))
                        .andReturn()
        ).isInstanceOfSatisfying(ServletException.class, ex ->
                assertThat(ex.getRootCause())
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("userId is required")
        );

        // then (DB에는 저장되지 않아야 함)
        List<CategoryEntity> all = categoryRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("createCategory - 존재하지 않는 userId로 호출 시 IllegalArgumentException 발생")
    void testCreateCategory_UserNotFound_Throws() {
        // given
        String name = "work";
        String missingUserId = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/category/")
                                .param("userId", missingUserId)
                                .param("name", name))
                        .andReturn()
        ).isInstanceOfSatisfying(ServletException.class, ex ->
                assertThat(ex.getRootCause())
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("User not found")
        );

        // then (DB에는 저장되지 않아야 함)
        List<CategoryEntity> all = categoryRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("updateCategory - 카테고리명 수정 성공")
    void testUpdateCategory_Success() throws Exception {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity category = saveFakeCategory(user, "work");
        CategorySummaryDto dto = new CategorySummaryDto();
        dto.setId(category.getId());
        dto.setUserId(user.getId());
        dto.setName("personal");

        // when & then
        mockMvc.perform(patch("/category/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId().toString()))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value("personal"));

        CategoryEntity updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("personal");
    }

    @Test
    @DisplayName("deleteCategory - 카테고리 삭제(soft delete) 성공")
    void testDeleteCategory_Success() throws Exception {
        // given
        UserEntity user = saveFakeUser();
        CategoryEntity category = saveFakeCategory(user, "work");

        // when & then
        mockMvc.perform(delete("/category/{categoryId}", category.getId()))
                .andExpect(status().isOk());

        CategoryEntity deleted = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
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

