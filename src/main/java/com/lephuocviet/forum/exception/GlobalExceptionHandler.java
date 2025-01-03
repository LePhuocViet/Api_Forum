package com.lephuocviet.forum.exception;

import com.lephuocviet.forum.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebException.class)
    ResponseEntity<ApiResponses> handleWebException(WebException e){
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponses.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponses> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        ErrorCode errorCode = ErrorCode.valueOf(e.getFieldError().getDefaultMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponses.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
}
