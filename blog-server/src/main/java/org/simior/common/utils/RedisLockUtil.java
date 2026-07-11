package org.simior.common.utils;

import lombok.RequiredArgsConstructor;
import org.simior.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具
 * <p>
 * 基于 SETNX + UUID + Lua 原子解锁实现，防止锁超时后误删他人锁。
 */
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁的 key
     * @return 锁标识（解锁时必须传入）
     * @throws BusinessException 获取失败时抛出
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 获取分布式锁（自定义超时）
     *
     * @param lockKey      锁的 key
     * @param timeoutSeconds 超时时间（秒），超时后自动释放防止死锁
     * @return 锁标识（解锁时必须传入）
     * @throws BusinessException 获取失败时抛出
     */
    public String tryLock(String lockKey, long timeoutSeconds) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, token, timeoutSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException("操作过于频繁，请稍后重试");
        }
        return token;
    }

    /**
     * 释放分布式锁（Lua 原子校验，仅释放自己持有的锁）
     *
     * @param lockKey 锁的 key
     * @param token   tryLock 返回的锁标识
     */
    public void unlock(String lockKey, String token) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
    }
}
