package com.it.zwx.studenthub_system.interceptor;

import com.it.zwx.studenthub_system.utils.JwtUtil;
import com.it.zwx.studenthub_system.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("进入拦截器");
        // 关键修复1：放行OPTIONS预检请求（跨域时浏览器会先发送OPTIONS请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 直接放行，不做Token校验
        }

        // 令牌的验证：借助request对象拿到token
        String token = request.getHeader("Authorization");

        // 关键修复2：处理Token为空的情况（避免解析null时报错）
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(401); // 未登录状态
            // 添加跨域响应头，避免浏览器拦截401响应
            addCorsHeaders(response);
            return false;
        }

        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Object uidObj = claims.get("id");
            Integer uid = uidObj instanceof Integer ? (Integer) uidObj : ((Number) uidObj).intValue();
            String pureToken = token.trim().startsWith("Bearer ") ? token.trim().substring("Bearer ".length()) : token.trim();
            Object loginFlag = redisTemplate.opsForValue().get("login:" + uid + ":" + pureToken);
            if (loginFlag == null) {
                response.setStatus(401);
                addCorsHeaders(response);
                return false;
            }
            ThreadLocalUtil.set(claims);
            return true;
        } catch (Exception e) {
            // 关键：打印完整异常堆栈（包含具体错误类型和原因）
            System.err.println("Token 解析失败，异常详情：");
            e.printStackTrace(); // 打印完整堆栈，不要省略
            response.setStatus(401);
            addCorsHeaders(response);
            return false;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据，防止内存泄漏
        ThreadLocalUtil.remove();
    }

    // 关键修复3：统一添加跨域响应头（确保前端能接收401等错误响应）
    private void addCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
}
