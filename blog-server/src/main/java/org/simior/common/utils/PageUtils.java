package org.simior.common.utils;

/**
 * 分页参数工具类
 * 统一处理分页参数的边界钳制，避免各 Controller 重复编写校验逻辑。
 */
public final class PageUtils {

    private PageUtils() {}

    public static long clamp(Long value, long min, long max) {
        long v = value == null ? min : value;
        return Math.min(Math.max(v, min), max);
    }

    public static int clamp(Integer value, int min, int max) {
        int v = value == null ? min : value;
        return Math.min(Math.max(v, min), max);
    }
}
