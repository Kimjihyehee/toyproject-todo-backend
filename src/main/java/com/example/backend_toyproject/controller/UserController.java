package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.UserDto;
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
     * 입력받는 필드 : name, nickName(unique)
     */
    @PostMapping
    public UserDto createUser(@RequestBody UserDto dto) {
        return userService.createUser(dto);
    }

    /*
     * 2. 유저 정보 조회(단일 유저)
     */
    @GetMapping("/{nickName}")
    public UserDto getUserTodo(@PathVariable String nickName) {
        return userService.getUserTodo(nickName);
    }

    /*
     * 3. 유저 정보 수정
     * 수정 가능한 필드 : name, nickName
     */
    @PatchMapping("/{nickName}")
    public UserDto updateUser(@PathVariable String nickName, @RequestBody UserDto dto) {
        return userService.updateUser(nickName, dto);
    }

    /*
     * 4. 유저 삭제
     */
    @DeleteMapping("/{nickName}")
    public UserDto deleteUser(@PathVariable String nickName) {
        return userService.deleteUser(nickName);
    }
}
