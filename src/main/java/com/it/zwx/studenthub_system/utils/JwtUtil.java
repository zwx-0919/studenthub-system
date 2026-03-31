package com.it.zwx.studenthub_system.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：生成 Token + 解析 Token（兼容 Bearer 前缀，优化异常处理）
 */
@Slf4j // 日志注解，替代 System.out，便于排查问题
public class JwtUtil {

    // 1. 优化：秘钥建议从配置文件读取（避免硬编码，这里先预留配置注入方式）
    // 生产环境注意：秘钥需复杂（如 32 位随机字符串），且通过环境变量/配置中心管理，不要明文写在代码里
    private static final String KEY = "la"; // 临时秘钥，后续建议改为：@Value("${jwt.secret}") private static String KEY;

    // 2. 优化：统一过期时间常量（便于维护，避免硬编码）
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 12; // 12小时过期（与原逻辑一致）

    /**
     * 生成 Token：接收业务数据（如用户ID、账号），返回带载荷的 JWT Token
     *
     * @param claims 业务数据（如 Map.of("id", 1, "userAccount", "admin")）
     * @return 纯 JWT Token（不含 Bearer 前缀）
     */
    public static String genToken(Map<String, Object> claims) {
        try {
            return JWT.create()
                    .withClaim("user", claims) // 载荷：业务数据封装在 "user" 字段下
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
                    .sign(Algorithm.HMAC256(KEY)); // HMAC256 算法 + 秘钥签名
        } catch (Exception e) {
            log.error("生成 Token 失败，业务数据：{}，异常信息：{}", claims, e.getMessage());
            throw new RuntimeException("Token 生成失败，请重试"); // 抛业务异常，便于上层捕获
        }
    }

    /**
     * 解析 Token：兼容「带 Bearer 前缀」和「纯 Token」两种格式，返回业务数据
     *
     * @param token 可能是：1. Bearer eyJhbGci...（Swagger 自动加前缀） 2. eyJhbGci...（纯 Token）
     * @return 载荷中的业务数据（如 {id=1, userAccount=admin}）
     */
    public static Map<String, Object> parseToken(String token) {
        // 步骤1：处理 Token 空值/空白（提前拦截，避免后续解析 NPE）
        if (token == null || token.trim().isEmpty()) {
            log.error("解析 Token 失败：Token 为空或空白");
            throw new RuntimeException("Token 不能为空");
        }

        // 步骤2：清理 Token 前后空格 + 去除 Bearer 前缀（核心！解决 Swagger 自动加前缀问题）
        String cleanToken = token.trim(); // 清理前后隐藏空格（如复制粘贴的不可见字符）
        if (cleanToken.startsWith("Bearer ")) { // 判断是否带 Bearer 前缀（注意：Bearer 后有一个空格）
            cleanToken = cleanToken.substring("Bearer ".length()); // 截取纯 Token（去掉 "Bearer " 前缀）
            log.debug("解析 Token：已去除 Bearer 前缀，纯 Token：{}", cleanToken);
        }

        // 步骤3：验证 Token 并解析业务数据（细分异常类型，便于排查问题）
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(KEY)) // 指定算法和秘钥（与生成时一致）
                    .build()
                    .verify(cleanToken); // 验证 Token 有效性（签名、过期时间等）

            // 解析载荷中的业务数据（与生成时的 "user" 字段对应）
            return decodedJWT.getClaim("user").asMap();

        } catch (SignatureVerificationException e) {
            // 异常1：签名错误（秘钥不匹配、Token 被篡改）
            log.error("解析 Token 失败：签名错误，Token：{}，异常：{}", cleanToken, e.getMessage());
            throw new RuntimeException("Token 签名无效，可能已被篡改");
        } catch (JWTDecodeException e) {
            // 异常3：Token 格式错误（如不是三段式 Base64 字符串）
            log.error("解析 Token 失败：格式错误，Token：{}，异常：{}", cleanToken, e.getMessage());
            throw new RuntimeException("Token 格式无效，请检查");
        } catch (Exception e) {
            // 异常4：其他未知错误（如算法不支持、载荷格式错误）
            log.error("解析 Token 失败：未知错误，Token：{}，异常：{}", cleanToken, e.getMessage());
            throw new RuntimeException("Token 验证失败，请联系管理员");
        }
    }
}