package org.simior.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.common.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException e) {
        log.error("用户未登录：{}", e.getMessage());
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供token";
            case NotLoginException.INVALID_TOKEN -> "token无效";
            case NotLoginException.TOKEN_TIMEOUT -> "token已过期";
            case NotLoginException.BE_REPLACED -> "token已被顶下线";
            case NotLoginException.KICK_OUT -> "token已被踢下线";
            default -> "当前会话未登录";
        };
        return Result.error(401, message);
    }

    /**
     * 处理 Sa-Token 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<String> handleNotPermissionException(NotPermissionException e) {
        log.error("权限不足：{}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    /**
     * 处理 Sa-Token 无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<String> handleNotRoleException(NotRoleException e) {
        log.error("角色不足：{}", e.getMessage());
        return Result.error(403, "角色不足");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定失败：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            if (split.length >= 3) {
                String value = split[2];
                // 去除引号包裹
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value.substring(1, value.length() - 1);
                }
                return Result.error(value + "已存在");
            }
        }
        log.error("SQL约束异常：{}", message);
        return Result.error("数据重复，请检查输入");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 只记录异常类型和消息，不记录完整栈追踪（避免泄露敏感信息）
        log.error("系统异常：{} - {}", e.getClass().getSimpleName(), e.getMessage());
        return Result.error("系统异常，请联系管理员");
    }
}
