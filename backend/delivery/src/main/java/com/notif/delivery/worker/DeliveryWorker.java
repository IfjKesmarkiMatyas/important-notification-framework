package com.notif.delivery.worker;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryStatus;
import com.notif.common.dto.delivery.ChannelResult;
import com.notif.common.entity.delivery.DeliveryJob;
import com.notif.delivery.channel.DeliveryChannel;
import com.notif.delivery.repository.DeliveryJobRepository;

@Component
public class DeliveryWorker {

    private static final Logger log = LoggerFactory.getLogger(DeliveryWorker.class);
    private static final int MAX_ATTEMPTS = 3;

    private final DeliveryJobRepository jobs;
    private final Map<DeliveryChannelType, DeliveryChannel> channels = new EnumMap<>(DeliveryChannelType.class);

    public DeliveryWorker(DeliveryJobRepository jobs, List<DeliveryChannel> channelList) {
        this.jobs = jobs;
        for (DeliveryChannel channel : channelList) {
            channels.put(channel.type(), channel);
        }
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void drain() {
        List<DeliveryJob> queued = jobs.findByStatusOrderByCreatedAtAsc(DeliveryStatus.queued);
        for (DeliveryJob job : queued) {
            process(job);
        }
    }

    private void process(DeliveryJob job) {
        job.setStatus(DeliveryStatus.sending);
        job.setLastAttemptAt(Instant.now());
        job.setAttemptCount(job.getAttemptCount() + 1);
        jobs.save(job);

        DeliveryChannel channel = channels.get(job.getChannel());
        if (channel == null) {
            fail(job, "Unknown channel: " + job.getChannel());
            return;
        }
        ChannelResult result = channel.send(job);
        if (result.ok()) {
            job.setStatus(DeliveryStatus.sent);
            job.setSentAt(Instant.now());
            job.setErrorMessage(null);
            jobs.save(job);
            return;
        }
        if (job.getAttemptCount() < MAX_ATTEMPTS && isTransient(result.errorMessage())) {
            log.warn("Delivery {} failed, will retry: {}", job.getId(), result.errorMessage());
            job.setStatus(DeliveryStatus.queued);
            job.setErrorMessage(result.errorMessage());
            jobs.save(job);
            return;
        }
        fail(job, result.errorMessage());
    }

    private void fail(DeliveryJob job, String message) {
        job.setStatus(DeliveryStatus.failed);
        job.setErrorMessage(message);
        jobs.save(job);
    }

    private static boolean isTransient(String message) {
        if (message == null) {
            return true;
        }
        String m = message.toLowerCase();
        return m.contains("timeout") || m.contains("connection") || m.contains("429") || m.contains("503");
    }
}
