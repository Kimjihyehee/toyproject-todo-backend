package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("getTodo - 단일 유저의 todo만 조회되고, dueDate ASC 정렬이 적용된다")
    void testGetTodo_FilterByUser_AndSortByDueDateAsc() {
        // given
        UserEntity userA = saveFakeUser();
        UserEntity userB = saveFakeUser();

        Timestamp due1 = Timestamp.valueOf("2026-01-01 10:00:00");
        Timestamp due2 = Timestamp.valueOf("2026-01-02 10:00:00");
        Timestamp dueB = Timestamp.valueOf("2026-01-01 09:00:00");

        TodoEntity a2 = saveTodo(userA, "A-2", due2);
        TodoEntity a1 = saveTodo(userA, "A-1", due1);
        saveTodo(userB, "B-1", dueB);

        // when
        List<TodoDto> result = todoService.getTodo(
                userA.getId(),
                SortType.DUE_DATE,
                SortDirection.ASC,
                0,
                10
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(dto -> assertThat(dto.getUserId()).isEqualTo(userA.getId()));
        assertThat(result).extracting(TodoDto::getId).containsExactly(a1.getId(), a2.getId());
    }

    @Test
    @DisplayName("getTodo - 페이징이 적용된다(page=0,size=1 / page=1,size=1)")
    void testGetTodo_Pagination() {
        // given
        UserEntity user = saveFakeUser();

        Timestamp due1 = Timestamp.valueOf("2026-01-01 10:00:00");
        Timestamp due2 = Timestamp.valueOf("2026-01-02 10:00:00");
        Timestamp due3 = Timestamp.valueOf("2026-01-03 10:00:00");

        TodoEntity t1 = saveTodo(user, "T1", due1);
        TodoEntity t2 = saveTodo(user, "T2", due2);
        saveTodo(user, "T3", due3);

        // when
        List<TodoDto> page0 = todoService.getTodo(user.getId(), SortType.DUE_DATE, SortDirection.ASC, 0, 1);
        List<TodoDto> page1 = todoService.getTodo(user.getId(), SortType.DUE_DATE, SortDirection.ASC, 1, 1);

        // then
        assertThat(page0).hasSize(1);
        assertThat(page1).hasSize(1);
        assertThat(page0.get(0).getId()).isEqualTo(t1.getId());
        assertThat(page1.get(0).getId()).isEqualTo(t2.getId());
    }

    @Test
    @DisplayName("getTodo - 존재하지 않는 유저면 예외가 발생한다")
    void testGetTodo_UserNotFound() {
        // given
        UUID missingUserId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(
                missingUserId,
                SortType.DUE_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    private TodoEntity saveTodo(UserEntity user, String title, Timestamp dueDate) {
        TodoEntity todo = new TodoEntity();
        todo.setUser(user);
        todo.setTitle(title);
        todo.setDueDate(dueDate);
        return todoRepository.saveAndFlush(todo);
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


