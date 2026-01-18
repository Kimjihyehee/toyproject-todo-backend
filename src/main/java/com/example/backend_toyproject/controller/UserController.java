package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.UserDto;
import com.example.backend_toyproject.model.dto.user.UserCreateDto;
import com.example.backend_toyproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    /*
     * 1. 유저 생성
     */
    @PostMapping
    public UserDto createUser(@RequestBody UserCreateDto userCreateDto) {
        return userService.createUser(userCreateDto);
    }

    /*
     * 2. 유저 정보 조회(단일 유저)
     */
    @GetMapping("/{nickName}")
    public UserDto getUserTodo(@PathVariable String nickName) {
        return userService.getUserTodo(nickName);
    }
}
