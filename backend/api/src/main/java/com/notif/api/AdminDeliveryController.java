package com.notif.api;

import com.notif.delivery.DeliveryChannelType;
import com.notif.delivery.DeliveryJob;
import com.notif.delivery.DeliveryService;
import com.notif.identity.TestDeliveryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/delivery")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;
    private final TestDeliveryService testDelivery;

    public AdminDeliveryController(DeliveryService deliveryService, TestDeliveryService testDelivery) {
        this.deliveryService = deliveryService;
        this.testDelivery = testDelivery;
    }

    @GetMapping("/jobs")
    public List<JobView> jobs() {
        return deliveryService.listRecent().stream().map(JobView::from).toList();
    }

    @PostMapping("/test")
    public TestResponse test(@Valid @RequestBody TestRequest request) {
        UUID id = testDelivery.send(request.channel(), request.recipient());
        return new TestResponse(id);
    }

    public record TestRequest(@NotNull DeliveryChannelType channel, String recipient) {}

    public record TestResponse(UUID jobId) {}

    public record JobView(
            UUID id,
            String purpose,
            String channel,
            String recipient,
            String status,
            String errorMessage,
            int attemptCount,
            Instant createdAt,
            Instant sentAt
    ) {
        static JobView from(DeliveryJob job) {
            return new JobView(
                    job.getId(),
                    job.getPurpose().name(),
                    job.getChannel().name(),
                    job.getRecipient(),
                    job.getStatus().name(),
                    job.getErrorMessage(),
                    job.getAttemptCount(),
                    job.getCreatedAt(),
                    job.getSentAt()
            );
        }
    }
}
