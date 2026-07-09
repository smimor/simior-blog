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
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度不能超过72个字符")
    private String password;

    /**
     * 验证码 ID（由 /v1/auth/captcha 接口返回）
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    /**
     * 验证码文本（用户输入）
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
