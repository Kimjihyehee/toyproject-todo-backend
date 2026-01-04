package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
     */
    public List<TodoDto> getTodo(
            UUID userId,
            List<String> categories,
            Priority priority,
            Boolean completed,
            String sort,
            String direction,
            int page,
            int size
    ) {
        // TODO : 필터/정렬/페이지네이션 로직 + repository 검색 메서드 구현이 필요
        return List.of(new TodoDto());
    }
}
