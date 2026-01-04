package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("createTodo - given/when/then으로 Todo 생성 성공")
    void testCreateTodo_Success() {
        // given
        UserEntity savedUser = saveFakeUser();

        TodoDto request = TodoDto.builder()
                .userId(savedUser.getId())
                .title("Test Todo")
                .description("Test Description")
                .dueDate(new Timestamp(System.currentTimeMillis() + 60_000))
                .priority(Priority.HIGH)
                .build();

        // when
        TodoDto created = todoService.createTodo(request);

        // then (반환 DTO)
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUserId()).isEqualTo(savedUser.getId());
        assertThat(created.getTitle()).isEqualTo(request.getTitle());
        assertThat(created.getDescription()).isEqualTo(request.getDescription());
        assertThat(created.getPriority()).isEqualTo(request.getPriority());

        // then (DB 저장 결과)
        TodoEntity savedEntity = todoRepository.findById(created.getId()).orElseThrow();
        assertThat(savedEntity.getTitle()).isEqualTo(request.getTitle());
        assertThat(savedEntity.getDescription()).isEqualTo(request.getDescription());
        assertThat(savedEntity.getPriority()).isEqualTo(request.getPriority());
        assertThat(savedEntity.getUser()).isNotNull();
        assertThat(savedEntity.getUser().getId()).isEqualTo(savedUser.getId());
    }

    private UserEntity saveFakeUser() {
        UserEntity user = new UserEntity();
        user.setName("test-user");
        user.setNickname("test-" + UUID.randomUUID());

        // nullable=false 컬럼들 방어적으로 세팅 (초기 단계 가라 데이터)
        Timestamp epoch = new Timestamp(0);
        user.setModifiedAt(epoch);
        user.setLastLoginAt(epoch);
        user.setDeletedAt(epoch);

        return userRepository.saveAndFlush(user);
    }
}


