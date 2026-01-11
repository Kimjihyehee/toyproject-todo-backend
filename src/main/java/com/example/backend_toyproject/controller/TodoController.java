package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.dto.todoUpdate.TodoUpdateRequestDTO;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.model.enums.TodoViewType;
import com.example.backend_toyproject.service.TodoService;
import jakarta.validation.Valid;
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
     * 필터 유형 : 년(year), 월(month), 일(day) 모두 제공 ex) 1999, 11,11로 param이
     * 정렬 유형 : 생성일순, 마감일순, 우선순위순, 완료/미완료순  - 1개만 선택 가능 & 기본값 : 생성일순
     * 정렬 방향 : ACS(오름차순), DESC(내림차순)        - null 가능 & 각 필터유형마다 정렬 기본값이 상이
     */
    @GetMapping("/{userId}")
    public List<TodoDto> getTodo(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MONTH", required = true) TodoViewType viewType,
            @RequestParam(required = true) Integer year,
            @RequestParam(required = false ) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(defaultValue = "CREATED_AT") SortType sortType,
            @RequestParam(required = false) SortDirection sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // sortType(정렬 항목)이 null일 경우,
        SortDirection direction =
                (sortDirection != null)
                        ? sortDirection
                        : sortType.getDefaultDirection();

        return todoService.getTodo(userId, viewType, year, month, day, sortType, direction, page, size);
    }

    /*
     * 3. 할일 수정 (단일 유저)
     * 수정 가능한 필드 : title, description, startDate, endDate, Priority, categories
     */
    @PatchMapping("/")
    public TodoDto updateTodo(@Valid @RequestBody TodoUpdateRequestDTO todoUpdateRequestDTO) {
        return todoService.updateTodo(todoUpdateRequestDTO);
    }

    /*
     * 4. 할일 단건 조회 (단일 유저)
     */
    @GetMapping("/{userId}/{todoId}")
    public TodoDto getTodoDetail(@PathVariable UUID userId, @PathVariable UUID todoId){
        return todoService.getTodoDetail(userId, todoId);
    }

    /*
     * 5. 할일 삭제
     */
    @DeleteMapping("{userId}/{todoId}")
    public TodoDto deleteTodo(@PathVariable UUID userId, @PathVariable UUID todoId){
        return todoService.deleteTodo(userId, todoId);
    }

}
