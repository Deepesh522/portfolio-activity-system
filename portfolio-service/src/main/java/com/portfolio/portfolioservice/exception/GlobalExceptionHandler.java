package com.portfolio.portfolioservice.exception;

import com.portfolio.shared.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fe -> ApiErrorResponse.FieldError.builder()
                                                .field(fe.getField())
                                                .message(fe.getDefaultMessage())
                                                .rejectedValue(fe.getRejectedValue())
                                                .build())
                                .toList();

                ApiErrorResponse response = ApiErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .message("Request validation failed")
                                .path(request.getRequestURI())
                                .timestamp(Instant.now())
                                .fieldErrors(fieldErrors)
                                .build();

                log.warn("Validation failed: {}", fieldErrors);
                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(InsufficientHoldingsException.class)
        public ResponseEntity<ApiErrorResponse> handleInsufficientHoldings(
                        InsufficientHoldingsException ex, HttpServletRequest request) {

                ApiErrorResponse response = ApiErrorResponse.builder()
                                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                                .error("Insufficient Holdings")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .timestamp(Instant.now())
                                .build();

                log.warn("Insufficient holdings: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneral(
                        Exception ex, HttpServletRequest request) {

                ApiErrorResponse response = ApiErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .message("An unexpected error occurred")
                                .path(request.getRequestURI())
                                .timestamp(Instant.now())
                                .build();

                log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
                return ResponseEntity.internalServerError().body(response);
        }
}
