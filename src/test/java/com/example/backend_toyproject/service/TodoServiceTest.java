package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.dto.todoUpdate.TodoUpdateRequestDTO;
import com.example.backend_toyproject.model.entity.CategoryEntity;
import com.example.backend_toyproject.model.entity.TodoCategoryMappingEntity;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.model.enums.TodoStatus;
import com.example.backend_toyproject.model.enums.TodoViewType;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("createTodo - given/when/then으로 Todo 생성 성공")
    void testCreateTodo_Success() {
        // given
        UserEntity savedUser = saveFakeUser();

        Timestamp start = Timestamp.valueOf("2026-01-01 09:00:00");
        Timestamp end = Timestamp.valueOf("2026-01-01 10:00:00");
        TodoDto request = TodoDto.builder()
                .userId(savedUser.getId())
                .title("Test Todo")
                .description("Test Description")
                .startDate(start)
                .endDate(end)
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
        assertThat(created.getStartDate()).isEqualTo(start);
        assertThat(created.getEndDate()).isEqualTo(end);

        // then (DB 저장 결과)
        TodoEntity savedEntity = todoRepository.findById(created.getId()).orElseThrow();
        assertThat(savedEntity.getTitle()).isEqualTo(request.getTitle());
        assertThat(savedEntity.getDescription()).isEqualTo(request.getDescription());
        assertThat(savedEntity.getPriority()).isEqualTo(request.getPriority());
        assertThat(savedEntity.getStartDate()).isEqualTo(start);
        assertThat(savedEntity.getEndDate()).isEqualTo(end);
        assertThat(savedEntity.getUser()).isNotNull();
        assertThat(savedEntity.getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("getTodo - 단일 유저의 todo만 조회되고, endDate(마감일) ASC 정렬이 적용된다")
    void testGetTodo_FilterByUser_AndSortByEndDateAsc() {
        // given
        UserEntity userA = saveFakeUser();
        UserEntity userB = saveFakeUser();

        Timestamp end1 = Timestamp.valueOf("2026-01-01 10:00:00");
        Timestamp end2 = Timestamp.valueOf("2026-01-02 10:00:00");
        Timestamp endB = Timestamp.valueOf("2026-01-01 09:00:00");

        TodoEntity a2 = saveTodo(userA, "A-2",
                Timestamp.valueOf("2026-01-02 09:00:00"),
                end2
        );
        TodoEntity a1 = saveTodo(userA, "A-1",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                end1
        );
        saveTodo(userB, "B-1",
                Timestamp.valueOf("2026-01-01 08:00:00"),
                endB
        );

        // when
        List<TodoDto> result = todoService.getTodo(
                userA.getId(),
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
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

        Timestamp end1 = Timestamp.valueOf("2026-01-01 10:00:00");
        Timestamp end2 = Timestamp.valueOf("2026-01-02 10:00:00");
        Timestamp end3 = Timestamp.valueOf("2026-01-03 10:00:00");

        TodoEntity t1 = saveTodo(user, "T1", Timestamp.valueOf("2026-01-01 09:00:00"), end1);
        TodoEntity t2 = saveTodo(user, "T2", Timestamp.valueOf("2026-01-02 09:00:00"), end2);
        saveTodo(user, "T3", Timestamp.valueOf("2026-01-03 09:00:00"), end3);

        // when
        List<TodoDto> page0 = todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                1
        );
        List<TodoDto> page1 = todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                1,
                1
        );

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
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getTodo - MONTH 조회: 기간이 겹치는 todo만 조회되고, 경계값(end==queryStart / start==queryEnd)은 제외된다")
    void testGetTodo_Month_OverlappingOnly_AndBoundaryExclusive() {
        // given
        UserEntity user = saveFakeUser();
        UserEntity otherUser = saveFakeUser();

        // query: 2026-01-01 00:00 ~ 2026-02-01 00:00
        Timestamp queryStart = Timestamp.valueOf("2026-01-01 00:00:00");
        Timestamp queryEnd = Timestamp.valueOf("2026-02-01 00:00:00");

        // 포함: queryStart를 걸치는 케이스 (end > queryStart && start < queryEnd)
        TodoEntity included1 = saveTodo(user, "included-1",
                Timestamp.valueOf("2025-12-31 23:00:00"),
                Timestamp.valueOf("2026-01-01 01:00:00")
        );
        TodoEntity included2 = saveTodo(user, "included-2",
                Timestamp.valueOf("2026-01-15 00:00:00"),
                Timestamp.valueOf("2026-01-20 00:00:00")
        );

        // 제외(경계): end == queryStart -> endDate > queryStart 조건 실패
        saveTodo(user, "excluded-boundary-end-eq-start",
                Timestamp.valueOf("2025-12-01 00:00:00"),
                queryStart
        );
        // 제외(경계): start == queryEnd -> startDate < queryEnd 조건 실패
        saveTodo(user, "excluded-boundary-start-eq-end",
                queryEnd,
                Timestamp.valueOf("2026-02-02 00:00:00")
        );
        // 제외(다른 유저): 기간이 겹쳐도 userId 다르면 제외
        saveTodo(otherUser, "excluded-other-user",
                Timestamp.valueOf("2026-01-10 00:00:00"),
                Timestamp.valueOf("2026-01-11 00:00:00")
        );

        // when
        List<TodoDto> result = todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        );

        // then
        assertThat(result).extracting(TodoDto::getId)
                .containsExactly(included1.getId(), included2.getId());
    }

    @Test
    @DisplayName("getTodo - DAY 조회: 기간이 겹치는 todo만 조회된다")
    void testGetTodo_Day_OverlappingOnly() {
        // given
        UserEntity user = saveFakeUser();

        // query: 2026-01-02 00:00 ~ 2026-01-03 00:00
        Timestamp queryStart = Timestamp.valueOf("2026-01-02 00:00:00");

        TodoEntity included = saveTodo(user, "included",
                Timestamp.valueOf("2026-01-01 23:00:00"),
                Timestamp.valueOf("2026-01-02 01:00:00")
        );
        saveTodo(user, "excluded-end-eq-start",
                Timestamp.valueOf("2026-01-01 00:00:00"),
                queryStart
        );

        // when
        List<TodoDto> result = todoService.getTodo(
                user.getId(),
                TodoViewType.DAY,
                2026,
                1,
                2,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        );

        // then
        assertThat(result).extracting(TodoDto::getId).containsExactly(included.getId());
    }

    @Test
    @DisplayName("getTodo - viewType=MONTH 인데 month=null & day!=null 이면 예외가 발생한다")
    void testGetTodo_InvalidParams_MonthWithoutMonthButWithDay() {
        // given
        UserEntity user = saveFakeUser();

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                null,
                2,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("getTodo - month/day 범위가 유효하지 않으면 예외가 발생한다")
    void testGetTodo_InvalidMonthOrDayRange() {
        // given
        UserEntity user = saveFakeUser();

        // month invalid
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                13,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );

        // day invalid
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.DAY,
                2026,
                1,
                32,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("getTodo - 유효하지 않은 날짜 조합(예: 2/30)이면 400(BAD_REQUEST) 예외가 발생한다")
    void testGetTodo_InvalidDateCombination_Feb30_ThrowsBadRequest() {
        // given
        UserEntity user = saveFakeUser();

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.DAY,
                2026,
                2,
                30,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("getTodo - 유효하지 않은 날짜 조합(예: 4/31)이면 400(BAD_REQUEST) 예외가 발생한다")
    void testGetTodo_InvalidDateCombination_Apr31_ThrowsBadRequest() {
        // given
        UserEntity user = saveFakeUser();

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.DAY,
                2026,
                4,
                31,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("getTodo - day만 있고 month가 없으면 400(BAD_REQUEST) 예외가 발생한다")
    void testGetTodo_DayWithoutMonth_ThrowsBadRequest() {
        // given
        UserEntity user = saveFakeUser();

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(
                user.getId(),
                TodoViewType.DAY,
                2026,
                null,
                2,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("updateTodo - 일부 필드(title/description/priority/completed) 수정이 성공한다")
    void testUpdateTodo_UpdateScalarFields_Success() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "old-title",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        todo.setDescription("old-desc");
        todoRepository.saveAndFlush(todo);

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setTitle("new-title");
        dto.setDescription("new-desc");
        dto.setPriority(Priority.URGENT);
        dto.setCompleted(true);

        // when
        TodoDto updated = todoService.updateTodo(dto);

        // then (반환 DTO)
        assertThat(updated.getId()).isEqualTo(todo.getId());
        assertThat(updated.getUserId()).isEqualTo(user.getId());
        assertThat(updated.getTitle()).isEqualTo("new-title");
        assertThat(updated.getDescription()).isEqualTo("new-desc");
        assertThat(updated.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();

        // then (DB)
        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("new-title");
        assertThat(saved.getDescription()).isEqualTo("new-desc");
        assertThat(saved.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(saved.isCompleted()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateTodo - completed=false로 변경 시 completedAt이 null로 초기화된다")
    void testUpdateTodo_CompletedFalse_ClearsCompletedAt() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        todo.setCompleted(true);
        todoRepository.saveAndFlush(todo);

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setCompleted(false);

        // when
        TodoDto updated = todoService.updateTodo(dto);

        // then (반환 DTO)
        assertThat(updated.isCompleted()).isFalse();
        assertThat(updated.getCompletedAt()).isNull();
        assertThat(updated.getStatus()).isEqualTo(TodoStatus.UPDATED);

        // then (DB)
        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.isCompleted()).isFalse();
        assertThat(saved.getCompletedAt()).isNull();
        assertThat(saved.getStatus()).isEqualTo(TodoStatus.UPDATED);
    }

    @Test
    @DisplayName("updateTodo - startDate가 endDate보다 이전이 아니면 400 예외가 발생한다")
    void testUpdateTodo_InvalidDateRange_ThrowsBadRequest() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setStartDate(Timestamp.valueOf("2026-01-02 10:00:00"));
        dto.setEndDate(Timestamp.valueOf("2026-01-02 10:00:00"));

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(dto))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                );
    }

    @Test
    @DisplayName("updateTodo - 날짜가 일부만 주어지면 나머지는 기존 값 유지된다")
    void testUpdateTodo_PartialDateUpdate_KeepsOtherDate() {
        // given
        UserEntity user = saveFakeUser();
        Timestamp originalStart = Timestamp.valueOf("2026-01-01 09:00:00");
        Timestamp originalEnd = Timestamp.valueOf("2026-01-01 10:00:00");
        TodoEntity todo = saveTodo(user, "todo", originalStart, originalEnd);

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setEndDate(Timestamp.valueOf("2026-01-01 11:00:00"));

        // when
        TodoDto updated = todoService.updateTodo(dto);

        // then
        assertThat(updated.getStartDate()).isEqualTo(originalStart);
        assertThat(updated.getEndDate()).isEqualTo(Timestamp.valueOf("2026-01-01 11:00:00"));

        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.getStartDate()).isEqualTo(originalStart);
        assertThat(saved.getEndDate()).isEqualTo(Timestamp.valueOf("2026-01-01 11:00:00"));
    }

    @Test
    @DisplayName("updateTodo - categories가 주어지면 기존 매핑이 전부 교체된다")
    void testUpdateTodo_CategoriesReplaced_Success() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        CategoryEntity work = saveCategory(user, "work");
        saveCategory(user, "home");

        // 기존 매핑 1개(work) 심어두기
        todo.getCategoryLinks().add(new TodoCategoryMappingEntity(todo, work));
        todoRepository.saveAndFlush(todo);

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setCategories(List.of("home"));

        // when
        TodoDto updated = todoService.updateTodo(dto);

        // then (반환 DTO)
        assertThat(updated.getCategories()).hasSize(1);
        assertThat(updated.getCategories().get(0).getName()).isEqualTo("home");

        // then (DB 매핑)
        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.getCategoryLinks()).hasSize(1);
        assertThat(saved.getCategoryLinks().get(0).getCategory().getName()).isEqualTo("home");
    }

    @Test
    @DisplayName("updateTodo - categories=[] 이면 기존 매핑이 전부 제거된다")
    void testUpdateTodo_EmptyCategories_ClearsMappings() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        CategoryEntity work = saveCategory(user, "work");
        todo.getCategoryLinks().add(new TodoCategoryMappingEntity(todo, work));
        todoRepository.saveAndFlush(todo);

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setCategories(List.of());

        // when
        TodoDto updated = todoService.updateTodo(dto);

        // then
        assertThat(updated.getCategories()).isEmpty();
        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.getCategoryLinks()).isEmpty();
    }

    @Test
    @DisplayName("updateTodo - 존재하지 않는 카테고리 이름이 포함되면 예외가 발생한다")
    void testUpdateTodo_CategoryNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        saveCategory(user, "work");

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setCategories(List.of("work", "missing"));

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Some categories not found");
    }

    @Test
    @DisplayName("updateTodo - 중복 카테고리 이름이 포함되면 예외가 발생한다")
    void testUpdateTodo_DuplicateCategories_Throws() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        saveCategory(user, "work");

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(user.getId());
        dto.setCategories(List.of("work", "work"));

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate category names");
    }

    @Test
    @DisplayName("updateTodo - 존재하지 않는 유저면 예외가 발생한다")
    void testUpdateTodo_UserNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(todo.getId());
        dto.setUserId(UUID.randomUUID());
        dto.setTitle("new-title");

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("updateTodo - 존재하지 않는 todo면 예외가 발생한다")
    void testUpdateTodo_TodoNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();

        TodoUpdateRequestDTO dto = new TodoUpdateRequestDTO();
        dto.setTodoId(UUID.randomUUID());
        dto.setUserId(user.getId());
        dto.setTitle("new-title");

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todo not found");
    }

    @Test
    @DisplayName("getTodoDetail - 단일 유저의 todo 단건 조회가 성공한다")
    void testGetTodoDetail_Success() {
        // given
        UserEntity user = saveFakeUser();
        Timestamp start = Timestamp.valueOf("2026-01-01 09:00:00");
        Timestamp end = Timestamp.valueOf("2026-01-01 10:00:00");
        TodoEntity todo = saveTodo(user, "todo", start, end);

        // when
        TodoDto result = todoService.getTodoDetail(user.getId(), todo.getId());

        // then
        assertThat(result.getId()).isEqualTo(todo.getId());
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getStartDate()).isEqualTo(start);
        assertThat(result.getEndDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("getTodoDetail - 존재하지 않는 유저면 예외가 발생한다")
    void testGetTodoDetail_UserNotFound_Throws() {
        // given
        UUID missingUserId = UUID.randomUUID();
        UUID anyTodoId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> todoService.getTodoDetail(missingUserId, anyTodoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getTodoDetail - 존재하지 않는 todo면 예외가 발생한다")
    void testGetTodoDetail_TodoNotFound_Throws() {
        // given
        UserEntity user = saveFakeUser();
        UUID missingTodoId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> todoService.getTodoDetail(user.getId(), missingTodoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todo not found");
    }

    @Test
    @DisplayName("getTodoDetail - 다른 유저의 todoId로 조회하면 예외가 발생한다")
    void testGetTodoDetail_OtherUsersTodo_Throws() {
        // given
        UserEntity owner = saveFakeUser();
        UserEntity otherUser = saveFakeUser();

        TodoEntity ownersTodo = saveTodo(
                owner,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );

        // when & then
        assertThatThrownBy(() -> todoService.getTodoDetail(otherUser.getId(), ownersTodo.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todo not found");
    }

    @Test
    @DisplayName("deleteTodo - deletedAt이 세팅된다(soft delete)")
    void testDeleteTodo_SetsDeletedAt() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );

        // when
        TodoDto deleted = todoService.deleteTodo(user.getId(), todo.getId());
        todoRepository.flush();

        // then (반환 DTO)
        assertThat(deleted.getId()).isEqualTo(todo.getId());
        assertThat(deleted.getUserId()).isEqualTo(user.getId());
        assertThat(deleted.getDeletedAt()).isNotNull();

        // then (DB)
        TodoEntity saved = todoRepository.findById(todo.getId()).orElseThrow();
        assertThat(saved.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteTodo - 삭제 후 getTodoDetail에서 제외된다")
    void testDeleteTodo_ThenGetTodoDetail_Throws() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        todoService.deleteTodo(user.getId(), todo.getId());
        todoRepository.flush();

        // when & then
        assertThatThrownBy(() -> todoService.getTodoDetail(user.getId(), todo.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todo not found");
    }

    @Test
    @DisplayName("deleteTodo - 삭제 후 getTodo(전체조회)에서 제외된다")
    void testDeleteTodo_ExcludedFromGetTodoList() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity willBeDeleted = saveTodo(
                user,
                "deleted",
                Timestamp.valueOf("2026-01-10 09:00:00"),
                Timestamp.valueOf("2026-01-10 10:00:00")
        );
        TodoEntity kept = saveTodo(
                user,
                "kept",
                Timestamp.valueOf("2026-01-11 09:00:00"),
                Timestamp.valueOf("2026-01-11 10:00:00")
        );
        todoService.deleteTodo(user.getId(), willBeDeleted.getId());
        todoRepository.flush();

        // when
        List<TodoDto> result = todoService.getTodo(
                user.getId(),
                TodoViewType.MONTH,
                2026,
                1,
                null,
                SortType.END_DATE,
                SortDirection.ASC,
                0,
                10
        );

        // then
        assertThat(result).extracting(TodoDto::getId)
                .contains(kept.getId())
                .doesNotContain(willBeDeleted.getId());
    }

    @Test
    @DisplayName("deleteTodo - 이미 삭제된 todo를 다시 삭제하면 예외가 발생한다(없는 것처럼)")
    void testDeleteTodo_SecondDelete_Throws() {
        // given
        UserEntity user = saveFakeUser();
        TodoEntity todo = saveTodo(
                user,
                "todo",
                Timestamp.valueOf("2026-01-01 09:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00")
        );
        todoService.deleteTodo(user.getId(), todo.getId());
        todoRepository.flush();

        // when & then
        assertThatThrownBy(() -> todoService.deleteTodo(user.getId(), todo.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todo not found");
    }

    private TodoEntity saveTodo(UserEntity user, String title, Timestamp startDate, Timestamp endDate) {
        TodoEntity todo = new TodoEntity();
        todo.setUser(user);
        todo.setTitle(title);
        todo.setStartDate(startDate);
        todo.setEndDate(endDate);
        return todoRepository.saveAndFlush(todo);
    }

    private CategoryEntity saveCategory(UserEntity user, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setUser(user);
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
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


