package cn.gp.smartparking.common;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtils {

    // JWT密钥
    private static final String SECRET_KEY = "smart_parking_secret_key_2026";

    // Token过期时间（24小时）
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return Token字符串
     */
    public static String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put(JWTPayload.ISSUED_AT, new Date());
        payload.put(JWTPayload.EXPIRES_AT, new Date(System.currentTimeMillis() + EXPIRATION_TIME));
        
        return JWTUtil.createToken(payload, SECRET_KEY.getBytes());
    }

    /**
     * 验证Token
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            return JWTUtil.verify(token, SECRET_KEY.getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token中的用户信息
     * @param token Token字符串
     * @return 用户信息Map
     */
    public static Map<String, Object> getTokenInfo(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token).setKey(SECRET_KEY.getBytes());
            return jwt.getPayloads();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Map<String, Object> info = getTokenInfo(token);
        if (info != null) {
            return Long.parseLong(info.get("userId").toString());
        }
        return null;
    }
}