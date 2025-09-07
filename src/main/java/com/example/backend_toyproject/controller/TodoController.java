package com.example.backend_toyproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {
    @GetMapping("/test")
    public String test() {
        return "Hello, toyProject _ Spring Boot!";
    }
}
