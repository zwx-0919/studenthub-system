package com.it.zwx.studenthub_system.utils;


import com.it.zwx.studenthub_system.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; // 发件人邮箱

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     */
    public void sendVerifyCode(String toEmail, String code) {
        // 1. 构建邮件内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // 发件人
        message.setTo(toEmail); // 收件人
        message.setSubject("【学生智能服务系统】忘记密码验证码");
        message.setText("你的验证码是：" + code + "，5分钟内有效，请及时修改密码。");

        // 2. 发送邮件
        try {
            mailSender.send(message);
            log.info("向{}发送验证码邮件成功，验证码：{}", toEmail, code);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new BusinessException("验证码邮件发送失败，请重试");
        }
    }
}
