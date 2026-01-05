package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
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
     * 정렬 유형 : 생성일순, 마감일순, 우선순위순, 완료/미완료순  - 1개만 선택 가능 & 기본값 : 생성일순
     * 정렬 방향 : ACS(오름차순), DESC(내림차순)        - null 가능 & 각 필터유형마다 정렬 기본값이 상이
     */
    @GetMapping("/{userId}")
    public List<TodoDto> getTodo(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "CREATED_AT") SortType sortType,
            @RequestParam(required = false) SortDirection sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SortDirection direction =
                (sortDirection != null)
                        ? sortDirection
                        : sortType.getDefaultDirection();
        return todoService.getTodo(userId, sortType, direction, page, size);
    }
}
