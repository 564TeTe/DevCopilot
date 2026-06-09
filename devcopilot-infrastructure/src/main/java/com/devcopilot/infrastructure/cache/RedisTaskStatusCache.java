package com.devcopilot.infrastructure.cache;

import com.devcopilot.application.port.TaskStatusCache;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTaskStatusCache implements TaskStatusCache {

    private static final Logger log = LoggerFactory.getLogger(RedisTaskStatusCache.class);

    private final StringRedisTemplate redisTemplate;

    public RedisTaskStatusCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(Long taskId, String statusJson) {
        try {
            redisTemplate.opsForValue().set(cacheKey(taskId), statusJson, Duration.ofHours(6));
        } catch (Exception ex) {
            log.warn("Failed to write task status cache, taskId={}", taskId, ex);
        }
    }

    @Override
    public Optional<String> get(Long taskId) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey(taskId)));
        } catch (Exception ex) {
            log.warn("Failed to read task status cache, taskId={}", taskId, ex);
            return Optional.empty();
        }
    }

    private String cacheKey(Long taskId) {
        return "devcopilot:task:" + taskId;
    }
}
