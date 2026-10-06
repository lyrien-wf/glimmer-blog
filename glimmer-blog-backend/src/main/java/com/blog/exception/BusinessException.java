package com.blog.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带明确的 HTTP 语义，避免所有失败都退化成 400。
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BusinessException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(HttpStatus.UNAUTHORIZED, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(HttpStatus.CONFLICT, message);
    }

    public static BusinessException internal(String message) {
        return new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static BusinessException internal(String message, Throwable cause) {
        return new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
