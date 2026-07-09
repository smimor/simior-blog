package org.simior.common.utils;

import cn.hutool.captcha.LineCaptcha;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

/**
 * 验证码生成工具
 * 基于 Hutool LineCaptcha 实现，生成带干扰线的验证码图片
 */
public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LEN = 4;
    private static final int LINE_COUNT = 5;

    /**
     * 生成验证码，同时返回文本和 Base64 图片
     *
     * @return Map 包含 "code"（验证码文本）和 "image"（Base64 图片）
     */
    public static Map<String, String> generate() {
        LineCaptcha captcha = new LineCaptcha(WIDTH, HEIGHT, CODE_LEN, LINE_COUNT);
        captcha.createCode();

        String code = captcha.getCode();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        captcha.write(bos);
        String image = "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());

        return Map.of("code", code, "image", image);
    }
}
