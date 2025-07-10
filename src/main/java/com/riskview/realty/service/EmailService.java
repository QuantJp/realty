package com.riskview.realty.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    // EmailService 클래스 로깅을 위한 Logger
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    // 이메일 발송을 위한 JavaMailSender 객체
    private JavaMailSender mailSender;

    // 이메일을 전송하는 메서드
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage(); // MimeMessage 객체 생성(HTML 형식의 이메일을 전송하기 위해)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true: multipart 메시지 사용(첨부 파일 포함 가능)

            helper.setTo(to); // 수신자 이메일
            helper.setFrom("${spring.mail.username}"); // 발신자 이메일
            helper.setSubject(subject); // 이메일 제목
            helper.setText(text, true); // 이메일 내용 (true: HTML 형식)
            mailSender.send(message); // 이메일 전송
            
            logger.info("Email sent successfully to {}", to); // 이메일 전송 성공 로깅
            
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage()); // 이메일 전송 실패 로깅
        }
    }
}
