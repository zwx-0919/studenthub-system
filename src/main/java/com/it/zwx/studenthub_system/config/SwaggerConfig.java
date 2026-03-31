package com.it.zwx.studenthub_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 定义 Bearer Token 安全方案名称
    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 全局启用 Token 认证（对业务接口生效）
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                // 配置安全方案（Token 格式说明）
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("请输入：Bearer + 空格 + Token（例如：Bearer eyJhbGciOiJIUzI1NiIs...）")
                        )
                )
                // 文档基础信息
                .info(new Info()
                        .title("高校学生智能服务系统 API")
                        .description("### 接口文档说明\n" +
                                "#### 核心功能模块：\n" +
                                "1. 用户管理（登录、角色权限、个人信息修改）\n" +
                                "2. 学业打卡（图书馆/自习室打卡、打卡记录查询）\n" +
                                "3. 请假申请（学生提交、辅导员审批、申请记录查询）\n" +
                                "4. 校园帖子（发布/查看/筛选、点赞/评论、管理员审核）\n" +
                                "5. 课程/考试（信息查看、考试提醒展示）\n\n" +
                                "#### 权限说明：\n" +
                                "✅ 无需Token：登录、注册接口\n" +
                                "🔒 需Token验证：所有业务功能接口（右上角「Authorize」输入Token）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("毕设技术支持")
                                .email("campus-support@example.com")
                                .url("http://localhost:8080")) // 可选：补充系统访问地址
                );
    }
}