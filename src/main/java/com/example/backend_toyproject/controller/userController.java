package com.example.backend_toyproject.controller;

import com.example.backend_toyproject.model.dto.UserDto;
import com.example.backend_toyproject.model.dto.user.UserCreateDto;
import com.example.backend_toyproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class userController {

    private final UserService userService;
    /*
     * 1. 유저 생성
     */
    @PostMapping
    public UserDto createUser(@RequestBody UserCreateDto userCreateDto) {
        return userService.createUser(userCreateDto);
    }
}
