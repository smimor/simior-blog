package org.simior.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.model.dto.LoginDTO;
import org.simior.model.dto.RegisterDTO;
import org.simior.model.vo.LoginVO;
import org.simior.model.vo.UserInfoVO;
import org.simior.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 权限认证控制器
 * <p>
 * 处理用户登录、注册、登出、Token 刷新及登录状态检查。
 * URL 前缀：/v1/auth。其中 login、register、check 为公开接口，
 * 其余接口需要用户已登录。
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数（用户名 + 密码）
     * @return Token 及用户基本信息
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }


    /**
     * 获取图形验证码
     *
     * @return captchaId（用于后续校验）和 captchaImage（Base64 图片）
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> result = authService.getCaptcha();
        return Result.success(result);
    }


    /**
     * 用户注册（注册成功后自动登录）
     *
     * @param registerDTO 注册参数
     * @return Token 及用户基本信息
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success("注册成功", loginVO);
    }

    /**
     * 用户登出（清除服务端 Token）
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户详细信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO userInfo = authService.getCurrentUser();
        return Result.success(userInfo);
    }

    /**
     * 刷新 Token（延长有效期）
     *
     * @return 新的 Token 信息
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken() {
        LoginVO loginVO = authService.refreshToken();
        return Result.success("Token 刷新成功", loginVO);
    }

    /**
     * 检查登录状态
     *
     * @return true=已登录，false=未登录
     */
    @GetMapping("/check")
    public Result<Boolean> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        return Result.success(isLogin);
    }
}
