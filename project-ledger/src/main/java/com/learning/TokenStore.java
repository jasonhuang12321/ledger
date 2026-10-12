package com.learning;

import java.time.Duration;
import java.util.Set;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private final StringRedisTemplate redis;

    public TokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(String jti, String username, long expireMs) {
        migrateIfSingleDevice(username);
        prune(username);
        Duration ttl = Duration.ofMillis(expireMs);
        redis.opsForValue().set(tokenKey(jti), username, ttl);
        redis.opsForSet().add(userKey(username), jti);
    }

    public boolean exists(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(tokenKey(jti)));
    }

    public void remove(String jti) {
        String username = redis.opsForValue().get(tokenKey(jti));
        redis.delete(tokenKey(jti));
        if (username != null) {
            migrateIfSingleDevice(username);
            redis.opsForSet().remove(userKey(username), jti);
        }
    }

    private void migrateIfSingleDevice(String username) {
        String key = userKey(username);
        if (redis.type(key) == DataType.STRING) {
            String old = redis.opsForValue().get(key);
            redis.delete(key);
            if (old != null && !old.isBlank()) {
                redis.opsForSet().add(key, old);
            }
        }
    }

    private void prune(String username) {
        Set<String> members = redis.opsForSet().members(userKey(username));
        if (members == null) {
            return;
        }
        for (String jti : members) {
            if (!exists(jti)) {
                redis.opsForSet().remove(userKey(username), jti);
            }
        }
    }

    private String tokenKey(String jti) {
        return "jwt:" + jti;
    }

    private String userKey(String username) {
        return "jwt-user:" + username;
    }
}
