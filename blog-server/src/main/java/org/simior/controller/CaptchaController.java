package org.simior.controller;

import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.common.utils.CaptchaUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 * <p>
 * 提供图形验证码的生成接口，验证码文本存入 Redis（TTL 5分钟），
 * 前端在登录时需携带 captchaId 和 captchaCode 完成验证。
 * URL 前缀：/v1/auth，公开接口无需登录。
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取图形验证码
     *
     * @return captchaId（用于后续校验）和 captchaImage（Base64 图片）
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        // 一次性生成验证码文本和图片（确保一致）
        Map<String, String> captcha = CaptchaUtil.generate();
        String code = captcha.get("code");
        String captchaImage = captcha.get("image");

        // 生成唯一 ID
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，5 分钟过期
        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        stringRedisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaImage", captchaImage);

        return Result.success(result);
    }
}
