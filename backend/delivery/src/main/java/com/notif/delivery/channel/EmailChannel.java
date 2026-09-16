package com.notif.delivery.channel;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.dto.delivery.ChannelResult;
import com.notif.common.entity.delivery.DeliveryJob;

@Component
public class EmailChannel implements DeliveryChannel {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailChannel(
            JavaMailSender mailSender,
            @Value("${notif.mail.from:Notif <noreply@notif.local>}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public DeliveryChannelType type() {
        return DeliveryChannelType.email;
    }

    @Override
    public ChannelResult send(DeliveryJob job) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(job.getRecipient());
            helper.setSubject(job.getSubject() == null ? "Notif" : job.getSubject());
            String text = job.getBodyText() == null ? "" : job.getBodyText();
            String html = job.getBodyHtml();
            if (html != null && !html.isBlank()) {
                helper.setText(text, html);
            } else {
                helper.setText(text, false);
            }
            mailSender.send(message);
            return ChannelResult.success();
        } catch (Exception ex) {
            return ChannelResult.failure(human(ex));
        }
    }

    private static String human(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? "email send failed" : m;
    }
}
