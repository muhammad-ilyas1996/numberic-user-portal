package com.numbericsuserportal.usermanagement.service.impl;

import com.numbericsuserportal.usermanagement.service.AuthEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class AuthEmailServiceImpl implements AuthEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@numbrics.com}")
    private String fromEmail;

    @Value("${app.auth.reset-password-url:http://localhost:3000/reset-password?token=}")
    private String resetPasswordUrl;

    @Override
    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        if (mailSender == null || toEmail == null || toEmail.trim().isEmpty()) {
            return false;
        }
        try {
            String resetLink = resetPasswordUrl + resetToken;
            String subject = "Reset Your Password";
            String htmlBody = buildPasswordResetEmailBody(resetLink);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail.trim());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    @Override
    public boolean sendUsernameReminderEmail(String toEmail, String username) {
        if (mailSender == null || toEmail == null || toEmail.trim().isEmpty()) {
            return false;
        }
        try {
            String subject = "Your Username Reminder";
            String htmlBody = buildUsernameReminderEmailBody(username);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail.trim());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    private String buildPasswordResetEmailBody(String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: #fff; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                ".warning { color: #dc3545; font-size: 14px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>You have requested to reset your password. Click the button below to reset it:</p>" +
                "<a href='" + resetLink + "' class='button'>Reset Password</a>" +
                "<p>Or copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all;'>" + resetLink + "</p>" +
                "<p class='warning'><strong>Important:</strong> This link will expire in 24 hours. If you did not request this password reset, please ignore this email.</p>" +
                "<p>For security reasons, do not share this link with anyone.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildUsernameReminderEmailBody(String username) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".username-box { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; font-size: 18px; font-weight: bold; }" +
                ".warning { color: #dc3545; font-size: 14px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>Username Reminder</h2>" +
                "<p>You have requested a reminder of your username. Here it is:</p>" +
                "<div class='username-box'>" + username + "</div>" +
                "<p class='warning'><strong>Security Reminder:</strong> Keep your username and password secure. Do not share them with anyone.</p>" +
                "<p>If you did not request this reminder, please contact support immediately.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
