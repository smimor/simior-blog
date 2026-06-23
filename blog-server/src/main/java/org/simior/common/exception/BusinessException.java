package org.simior.common.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
