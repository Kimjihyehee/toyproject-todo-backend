package com.example.backend_toyproject.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Jackson이 클래스를 역직렬화 대상(JSON → Java)으로 사용할 때, RestControllerAdvice를 통해 걸러진 예외처리
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleTimestampParseError(
            HttpMessageNotReadableException ex
    ) {
        log.error("Timestamp parsing error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body("timestamp 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm:ss)");
    }
}