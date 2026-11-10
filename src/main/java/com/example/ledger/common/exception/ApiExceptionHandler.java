package com.example.ledger.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/** 엔티티 규칙 위반(IllegalArgumentException)과 업로드 크기 초과를 400 으로 바꾼다. */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("message", e.getMessage());
    }

    /** application.yml 의 업로드 한도를 넘은 요청 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handleTooLarge(MaxUploadSizeExceededException e) {
        return Map.of("message", "이미지는 한 장에 5MB 이하여야 합니다");
    }
}
