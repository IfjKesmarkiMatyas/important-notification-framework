package com.notif.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryWorkerTest {

    @Mock
    private DeliveryJobRepository jobs;

    @Mock
    private DeliveryChannel email;

    private DeliveryWorker worker;

    @BeforeEach
    void setUp() {
        when(email.type()).thenReturn(DeliveryChannelType.email);
        worker = new DeliveryWorker(jobs, List.of(email));
    }

    @Test
    void marksJobSentOnSuccess() {
        DeliveryJob job = queued(DeliveryChannelType.email);
        when(jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued)).thenReturn(List.of(job));
        when(email.send(job)).thenReturn(ChannelResult.success());

        worker.drain();

        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.sent);
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getSentAt()).isNotNull();
        assertThat(job.getErrorMessage()).isNull();
    }

    @Test
    void retriesTransientFailureUntilLimit() {
        DeliveryJob job = queued(DeliveryChannelType.email);
        when(jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued)).thenReturn(List.of(job));
        when(email.send(job)).thenReturn(ChannelResult.failure("connection timeout"));

        worker.drain();

        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.queued);
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getErrorMessage()).contains("timeout");
    }

    @Test
    void failsPermanentlyAfterMaxTransientAttempts() {
        DeliveryJob job = queued(DeliveryChannelType.email);
        job.setAttemptCount(2);
        when(jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued)).thenReturn(List.of(job));
        when(email.send(job)).thenReturn(ChannelResult.failure("503"));

        worker.drain();

        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.failed);
        assertThat(job.getAttemptCount()).isEqualTo(3);
        assertThat(job.getErrorMessage()).isEqualTo("503");
    }

    @Test
    void failsImmediatelyOnNonTransientError() {
        DeliveryJob job = queued(DeliveryChannelType.email);
        when(jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued)).thenReturn(List.of(job));
        when(email.send(job)).thenReturn(ChannelResult.failure("Slack bot token is not configured"));

        worker.drain();

        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.failed);
        assertThat(job.getErrorMessage()).contains("Slack bot token");
    }

    @Test
    void failsUnknownChannelWithoutSending() {
        DeliveryJob job = queued(DeliveryChannelType.pushover);
        when(jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued)).thenReturn(List.of(job));

        worker.drain();

        assertThat(job.getStatus()).isEqualTo(DeliveryStatus.failed);
        assertThat(job.getErrorMessage()).contains("Unknown channel");
        verify(email, never()).send(job);
    }

    private static DeliveryJob queued(DeliveryChannelType channel) {
        DeliveryJob job = new DeliveryJob();
        job.setId(UUID.randomUUID());
        job.setChannel(channel);
        job.setPurpose(DeliveryPurpose.TEST);
        job.setRecipient("target");
        job.setStatus(DeliveryStatus.queued);
        job.setAttemptCount(0);
        return job;
    }
}
