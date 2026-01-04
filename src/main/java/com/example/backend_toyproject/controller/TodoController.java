package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.enums.Priority;
import com.example.backend_toyproject.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todo")
@Validated
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /*
     * 1. 할일 생성
     */
    @PostMapping("/")
    public TodoDto createTodo(@RequestBody TodoDto todoDto) {
        return todoService.createTodo(todoDto);
    }

    /*
     * 2. 할일 전체 조회(단일 유저)
     */
    @GetMapping("/{userId}")
    public List<TodoDto> getTodo(
            @PathVariable UUID userId,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return todoService.getTodo(userId, categories, priority, completed, sort, direction, page, size);
    }
}
