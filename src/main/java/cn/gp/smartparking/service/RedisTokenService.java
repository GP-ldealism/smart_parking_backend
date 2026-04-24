package cn.gp.smartparking.service;

import cn.gp.smartparking.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisTokenService {

    private static final String TOKEN_PREFIX = "smart_parking:user";
    private static final String USER_TOKEN_PREFIX = "smart_parking:user_token:";
    private static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String token, User user) {
        String tokenKey = TOKEN_PREFIX + token;
        String userTokenKey = USER_TOKEN_PREFIX + user.getId();

        redisTemplate.opsForValue().set(tokenKey, user, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userTokenKey, token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        log.info("Token已存储到Redis - userId: {}, token: {}", user.getId(), token);
    }

    public User getUserByToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userObj = redisTemplate.opsForValue().get(tokenKey);
        if (userObj != null) {
            redisTemplate.expire(tokenKey, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
            if (userObj instanceof User) {
                return (User) userObj;
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(tokenKey);
        return exists != null && exists;
    }

    public void removeToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userObj = redisTemplate.opsForValue().get(tokenKey);
        if (userObj instanceof User) {
            User user = (User) userObj;
            String userTokenKey = USER_TOKEN_PREFIX + user.getId();
            redisTemplate.delete(userTokenKey);
        }
        redisTemplate.delete(tokenKey);
        log.info("Token已从Redis移除 - token: {}", token);
    }

    public void removeTokenByUserId(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        Object tokenObj = redisTemplate.opsForValue().get(userTokenKey);
        if (tokenObj != null) {
            String token = tokenObj.toString();
            String tokenKey = TOKEN_PREFIX + token;
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userTokenKey);
            log.info("用户Token已从Redis移除 - userId: {}", userId);
        }
    }

    public String getTokenByUserId(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        Object tokenObj = redisTemplate.opsForValue().get(userTokenKey);
        return tokenObj != null ? tokenObj.toString() : null;
    }
}