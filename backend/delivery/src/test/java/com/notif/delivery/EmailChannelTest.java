package com.notif.delivery;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailChannelTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendReturnsSuccessWhenMailSenderAcceptsMessage() {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);
        EmailChannel channel = new EmailChannel(mailSender, "Notif <noreply@notif.local>");
        DeliveryJob job = job("ada@notif.local", "<p>hi</p>");

        ChannelResult result = channel.send(job);

        assertThat(channel.type()).isEqualTo(DeliveryChannelType.email);
        assertThat(result.ok()).isTrue();
        verify(mailSender).send(mime);
    }

    @Test
    void sendReturnsFailureWhenMailSenderThrows() {
        when(mailSender.createMimeMessage()).thenThrow(new IllegalStateException("SMTP down"));
        EmailChannel channel = new EmailChannel(mailSender, "noreply@notif.local");

        ChannelResult result = channel.send(job("ada@notif.local", null));

        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("SMTP down");
    }

    @Test
    void sendUsesGenericMessageWhenExceptionHasNoText() {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doThrow(new RuntimeException()).when(mailSender).send(any(MimeMessage.class));
        EmailChannel channel = new EmailChannel(mailSender, "noreply@notif.local");

        ChannelResult result = channel.send(job("ada@notif.local", "plain"));

        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("email send failed");
    }

    private static DeliveryJob job(String to, String html) {
        DeliveryJob job = new DeliveryJob();
        job.setRecipient(to);
        job.setSubject("Notif teszt");
        job.setBodyText("hello");
        job.setBodyHtml(html);
        return job;
    }
}
