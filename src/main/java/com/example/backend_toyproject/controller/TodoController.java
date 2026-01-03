package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todo")
@Validated
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /*
     * 1. 할일 생성
     */
    @PostMapping("/create")
    public TodoDto createTodo(TodoDto todoDto) {
        return todoService.createTodo(todoDto);
    }
}
