package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    /*
    * 1. 할일 생성
    * */
    public TodoDto createTodo(TodoDto todoDto) {
        // userId null 체크
        UUID userId = todoDto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to create a todo");
        }
        // 사용자인지 체크
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // DTO -> Entity 변환
        TodoEntity todoEntity = new TodoEntity(todoDto);
        // user 정보 Entity에 연결
        todoEntity.setUser(user);
        // 저장
        TodoEntity savedTodoEntity = todoRepository.save(todoEntity);
        return new TodoDto(savedTodoEntity);
    }

    /*
     * 2. 할일 전체 조회(단일 유저)
     * 정렬 유형 : 생성일순, 마감일순, 우선순위순, 완료/미완료순  - 1개만 선택 가능 & 기본값 : 생성일순
     * 정렬 방향 : ACS(오름차순), DESC(내림차순)        - null 가능 & 각 필터유형마다 정렬 기본값이 상이
     */
    public List<TodoDto> getTodo(
            UUID userId,
            SortType sortType,
            SortDirection direction,
            int page,
            int size
    ) {
        // 1. 사용자 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2. 정렬 필드 결정
        String sortField = resolveSortField(sortType);
        // 3. 정렬 방향 결정
        Sort.Direction sortDirection = resolveDirection(direction);
        // 4. Sort + Pageable 생성
        Sort sort = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 5. 조회
        Page<TodoEntity> todoPage = todoRepository.findByUser_Id(userId, pageable);

        // 6. Entity -> DTO 변환된 값으로 return
        return todoPage.getContent()
                .stream()
                .map(TodoDto::new)
                .toList();
    }

    /*
     * enum으로 표현된 “정렬 의미” -> 실제 DB(Entity) 필드명으로 변환하는 함수
     */
    private String resolveSortField(SortType sortType) {
        return switch (sortType) {
            case CREATED_AT ->  "createdAt";
            case DUE_DATE ->  "dueDate";
            case PRIORITY ->  "priority";
            case COMPLETED ->  "completed";
        };
    }

    /*
     * enum 정렬 방향 -> Spring Direction 변환하는 함수
     */
    private Sort.Direction resolveDirection(SortDirection direction) {
        return direction == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

}
