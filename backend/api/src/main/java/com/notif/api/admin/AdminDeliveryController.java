package com.notif.api.admin;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.delivery.JobView;
import com.notif.common.dto.delivery.TestRequest;
import com.notif.common.dto.delivery.TestResponse;
import com.notif.delivery.service.DeliveryService;
import com.notif.identity.service.TestDeliveryService;

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
}
