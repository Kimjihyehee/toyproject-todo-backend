package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.entity.TodoEntity;
import com.example.backend_toyproject.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;

    /*
    * 1. 할일 생성
    * */
    public TodoDto createTodo(TodoDto todoDto) {
        var todoEntity = new TodoEntity(todoDto);
        TodoEntity a = todoRepository.save(todoEntity); // 저장
        return new TodoDto(a);
    };
}
