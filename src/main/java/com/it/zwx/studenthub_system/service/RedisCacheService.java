package com.it.zwx.studenthub_system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // 使用自定义ObjectMapper

    @Autowired
    public RedisCacheService(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper // 明确注入
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // 原有方法：设置缓存（保留）
    public void setCache(String key, Object value, long timeout) {
        if (key == null || key.isEmpty() || value == null) {
            log.warn("缓存设置失败：key或value为空（key={}）", key);
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
        log.debug("缓存设置成功：key={}，过期时间={}分钟", key, timeout);
    }

    // 原有方法：获取缓存（适用于非泛型简单类型）
    public Object getCache(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("缓存获取失败：key为空");
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T getCache(String key, TypeReference<T> typeReference) {
        try {
            Object cacheValue = redisTemplate.opsForValue().get(key);
            if (cacheValue == null) return null;

            // 直接转换（避免二次序列化）
            return objectMapper.convertValue(cacheValue, typeReference);
        } catch (Exception e) {
            log.error("泛型缓存反序列化失败：key={}", key, e);
            redisTemplate.delete(key);
            return null;
        }
    }

    // 原有方法：删除单个缓存（保留）
    public void deleteCache(String key) {
        if (key == null || key.isEmpty()) {
            log.warn("缓存删除失败：key为空");
            return;
        }
        boolean success = redisTemplate.delete(key);
        if (success) {
            log.debug("单个缓存删除成功：key={}", key);
        } else {
            log.debug("单个缓存删除失败（key不存在）：key={}", key);
        }
    }

    // 原有方法：批量删除通配符缓存（保留）
    public void deleteCacheByPattern(String keyPattern) {
        if (keyPattern == null || keyPattern.isEmpty()) {
            log.warn("批量缓存删除失败：keyPattern为空");
            return;
        }
        try {
            Set<String> matchedKeys = redisTemplate.keys(keyPattern);
            if (matchedKeys == null || matchedKeys.isEmpty()) {
                log.debug("批量缓存删除：无匹配的缓存键（规则={}）", keyPattern);
                return;
            }
            long deleteCount = redisTemplate.delete(matchedKeys);
            log.info("批量缓存删除成功：规则={}，匹配键数={}，实际删除数={}",
                    keyPattern, matchedKeys.size(), deleteCount);
        } catch (Exception e) {
            log.error("批量缓存删除异常（规则={}）", keyPattern, e);
        }
    }

}
