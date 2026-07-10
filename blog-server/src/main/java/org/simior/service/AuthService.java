package org.simior.service;

import org.simior.model.dto.LoginDTO;
import org.simior.model.dto.RegisterDTO;
import org.simior.model.vo.LoginVO;
import org.simior.model.vo.UserInfoVO;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取图形验证码
     *
     * @return captchaId（用于后续校验）和 captchaImage（Base64 图片）
     */
    Map<String, String> getCaptcha();

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册结果
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUser();

    /**
     * 刷新 Token
     *
     * @return 新的 Token 信息
     */
    LoginVO refreshToken();
}
