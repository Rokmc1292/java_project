package com.example.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * - 모든 Controller에서 발생하는 예외를 통일된 형식으로 처리
 * - 사용자 친화적인 에러 메시지 제공
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리 (IllegalArgumentException)
     * - 잘못된 입력값, 중복 데이터 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                e.getMessage()
        );
    }

    /**
     * 리소스를 찾을 수 없는 경우 (RuntimeException)
     * - 존재하지 않는 게시글, 사용자 등
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        // 에러 메시지에 "찾을 수 없습니다"가 포함되어 있으면 404 반환
        if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
            return createErrorResponse(
                    HttpStatus.NOT_FOUND,
                    "NOT_FOUND",
                    e.getMessage()
            );
        }

        // 그 외의 경우 500 반환
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다."
        );
    }

    /**
     * 권한 없음 예외 처리 (AccessDeniedException)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        return createErrorResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "접근 권한이 없습니다."
        );
    }

    /**
     * 모든 예외의 최종 처리 (Exception)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        // 개발 환경에서는 스택 트레이스 로깅
        e.printStackTrace();

        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "예기치 않은 오류가 발생했습니다."
        );
    }

    /**
     * 에러 응답 생성 헬퍼 메서드
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status,
            String error,
            String message) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, status);
    }
}