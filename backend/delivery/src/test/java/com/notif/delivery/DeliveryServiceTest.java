package com.notif.delivery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryJobRepository jobs;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private DeliveryService service;

    @Test
    void enqueueInviteRendersHungarianTemplate() {
        when(jobs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(templateEngine.process(eq("invite-email"), any(Context.class))).thenReturn("<html>invite</html>");

        UUID id = service.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.INVITE,
                DeliveryChannelType.email,
                "ada@notif.local",
                "hu",
                null,
                null,
                null,
                Map.of("inviteUrl", "http://localhost:4200/invite/abc", "inviterName", "Admin")
        ));

        ArgumentCaptor<DeliveryJob> captor = ArgumentCaptor.forClass(DeliveryJob.class);
        verify(jobs).save(captor.capture());
        DeliveryJob job = captor.getValue();
        assertThat(id).isEqualTo(job.getId());
        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.queued);
        assertThat(job.getSubject()).isEqualTo("Meghívó a Notifra");
        assertThat(job.getBodyText()).contains("http://localhost:4200/invite/abc");
        assertThat(job.getBodyHtml()).isEqualTo("<html>invite</html>");
    }

    @Test
    void enqueueInviteEnglishUsesEnglishSubject() {
        when(jobs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(templateEngine.process(eq("invite-email"), any(Context.class))).thenReturn("<html>en</html>");

        service.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.INVITE,
                DeliveryChannelType.email,
                "ben@notif.local",
                "en",
                null,
                null,
                null,
                Map.of("inviteUrl", "http://localhost:4200/invite/tok", "inviterName", "Admin")
        ));

        ArgumentCaptor<DeliveryJob> captor = ArgumentCaptor.forClass(DeliveryJob.class);
        verify(jobs).save(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("You're invited to Notif");
        assertThat(captor.getValue().getBodyText()).contains("invited you");
    }

    @Test
    void enqueueTestRendersChannelTemplateAndDefaultsLocale() {
        when(jobs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(templateEngine.process(eq("test-email"), any(Context.class))).thenReturn("<p>test</p>");

        service.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.TEST,
                DeliveryChannelType.slack,
                "C123",
                "  ",
                null,
                null,
                null,
                Map.of()
        ));

        ArgumentCaptor<DeliveryJob> captor = ArgumentCaptor.forClass(DeliveryJob.class);
        verify(jobs).save(captor.capture());
        DeliveryJob job = captor.getValue();
        assertThat(job.getLocale()).isEqualTo("hu");
        assertThat(job.getSubject()).isEqualTo("Notif teszt");
        assertThat(job.getBodyText()).contains("slack");
        assertThat(job.getBodyHtml()).isEqualTo("<p>test</p>");
    }

    @Test
    void enqueueKeepsExplicitBodies() {
        when(jobs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.enqueue(new EnqueueDeliveryCommand(
                DeliveryPurpose.ALERT,
                DeliveryChannelType.email,
                "ada@notif.local",
                "hu",
                "Földrengés",
                "M6.2",
                "<p>M6.2</p>",
                Map.of()
        ));

        ArgumentCaptor<DeliveryJob> captor = ArgumentCaptor.forClass(DeliveryJob.class);
        verify(jobs).save(captor.capture());
        DeliveryJob job = captor.getValue();
        assertThat(job.getSubject()).isEqualTo("Földrengés");
        assertThat(job.getBodyText()).isEqualTo("M6.2");
        assertThat(job.getBodyHtml()).isEqualTo("<p>M6.2</p>");
    }

    @Test
    void latestInviteDelegatesToRepository() {
        DeliveryJob job = new DeliveryJob();
        when(jobs.findFirstByPurposeAndRecipientOrderByCreatedAtDesc(DeliveryPurpose.INVITE, "ada@notif.local"))
                .thenReturn(Optional.of(job));

        assertThat(service.latestInvite("ada@notif.local")).contains(job);
    }

    @Test
    void listRecentDelegatesToRepository() {
        when(jobs.findTop100ByOrderByCreatedAtDesc()).thenReturn(List.of());
        assertThat(service.listRecent()).isEmpty();
    }
}
