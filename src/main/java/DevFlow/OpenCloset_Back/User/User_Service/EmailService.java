package DevFlow.OpenCloset_Back.User.User_Service;

import DevFlow.OpenCloset_Back.User.entity.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(toEmail)
                .subject("[OpenCloset] 임시 비밀번호 안내")
                .message(buildEmailContent(tempPassword))
                .build();

        sendEmail(emailMessage);
    }

    private void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom("opencloset2026@gmail.com");
            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getMessage(), true); // HTML 형식

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildEmailContent(String tempPassword) {
        return """
                <div style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 30px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">🔐 임시 비밀번호 안내</h2>
                    <p style="color: #555; font-size: 14px; line-height: 1.8;">
                        안녕하세요, <strong>OpenCloset</strong>입니다.<br/>
                        요청하신 임시 비밀번호를 안내드립니다.
                    </p>
                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 24px; font-weight: bold; color: #2196F3; letter-spacing: 3px;">%s</span>
                    </div>
                    <p style="color: #999; font-size: 12px; line-height: 1.6;">
                        • 로그인 후 반드시 비밀번호를 변경해주세요.<br/>
                        • 본인이 요청하지 않은 경우, 이 메일을 무시해주세요.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>
                    <p style="color: #bbb; font-size: 11px; text-align: center;">© 2026 OpenCloset. All rights reserved.</p>
                </div>
                """
                .formatted(tempPassword);
    }
}
