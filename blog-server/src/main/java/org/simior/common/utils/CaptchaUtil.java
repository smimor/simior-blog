package org.simior.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具
 * 生成4位纯数字验证码图片
 */
public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LEN = 4;
    private static final Random RANDOM = new Random();

    // 背景色
    private static final Color BG_COLOR = new Color(245, 247, 250);
    // 干扰线颜色
    private static final Color LINE_COLOR = new Color(200, 200, 200);
    // 字体颜色池
    private static final Color[] FONT_COLORS = {
            new Color(76, 110, 245),   // 主色
            new Color(56, 178, 172),   // 青绿
            new Color(237, 137, 54),   // 橙色
            new Color(229, 62, 62),    // 红色
            new Color(159, 122, 234),  // 紫色
            new Color(49, 130, 206),   // 蓝色
    };

    /**
     * 生成4位纯数字验证码
     *
     * @return 验证码字符串
     */
    public static String generateCode() {
        return String.format("%04d", RANDOM.nextInt(10000));
    }

    /**
     * 根据验证码生成图片并返回 Base64
     *
     * @param code 4位数字验证码
     * @return data:image/png;base64,xxx 格式字符串
     */
    public static String generateBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // 背景
        g.setColor(BG_COLOR);
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 10, 10);

        // 干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(LINE_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200), 100));
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }

        // 绘制验证码字符
        Font font = new Font("SansSerif", Font.BOLD, 26);
        g.setFont(font);
        int startX = 18;
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(FONT_COLORS[RANDOM.nextInt(FONT_COLORS.length)]);
            // 随机旋转
            double theta = Math.toRadians(RANDOM.nextInt(30) - 15);
            int x = startX + i * 25;
            int y = 28 + RANDOM.nextInt(6) - 3;
            g.rotate(theta, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x, y);
        }

        g.dispose();

        // 转 Base64
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
    }
}
