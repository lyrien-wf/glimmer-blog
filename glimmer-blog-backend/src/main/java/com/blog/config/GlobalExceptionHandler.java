package com.blog.config;

import com.blog.dto.ApiResponse;
import com.blog.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：使用异常自身携带的状态码（404 / 409 / 401 ...），
     * 让前端能按语义区分"不存在""已存在""未登录"。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getStatus().value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    /** 路径/查询参数类型不匹配，例如 /api/admin/articles/abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ApiResponse.error(400, "参数类型不正确：" + ex.getName());
    }

    /** 缺少必要的查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        return ApiResponse.error(400, "缺少必要参数：" + ex.getParameterName());
    }

    /**
     * 请求体无法解析（JSON 语法错误、字段类型不匹配等）。
     * 必须显式声明：否则会落到 Exception 兜底分支变成 500。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error(400, "请求体格式错误");
    }

    /**
     * 上传体积超限。必须单独处理：否则会落到 Exception 兜底分支，
     * 把框架原文（Maximum upload size exceeded）或 500 直接透给前端。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Void> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ApiResponse.error(413, "文件过大，请压缩后重试");
    }

    /**
     * 唯一约束等数据库约束冲突：多为并发下的重名，属于可预期的客户端错误，
     * 返回 409 而不是 500，同时把真实原因写进日志。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("数据约束冲突: {}", ex.getMostSpecificCause().getMessage());
        return ApiResponse.error(409, "数据已存在或违反约束，请检查后重试");
    }

    /**
     * 兜底：未预期的异常一律 500，并记录堆栈。
     * 不再用 message 里是否含 "error"/"Exception" 来猜是否需要脱敏；
     * 可预期的业务失败请用 BusinessException 表达。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("未处理的异常", ex);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
