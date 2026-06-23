package org.simior.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求参数
 *
 * <p>用于 {@code POST /v1/auth/login} 接口。
 * 用户名和密码均为必填项；密码长度限制为 72 个字符，因为 BCrypt 算法会截断超过 72 字节的输入，超出部分不参与哈希计算。</p>
 */
@Data
public class LoginDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(max = 72, message = "密码长度不能超过72个字符")
    private String password;
}
