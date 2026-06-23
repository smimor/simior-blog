package org.simior.model.vo;

import lombok.Data;

/**
 * 登录返回结果
 */
@Data
public class LoginVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * Token
     */
    private String token;

    /**
     * Token 名称
     */
    private String tokenName;

    /**
     * Token 有效期（秒）
     */
    private Long tokenTimeout;
}
