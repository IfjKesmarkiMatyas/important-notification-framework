package com.notif.delivery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryJobRepository extends JpaRepository<DeliveryJob, UUID> {
    List<DeliveryJob> findTop100ByOrderByCreatedAtDesc();

    List<DeliveryJob> findByStatusOrderByCreatedAtAsc(DeliveryStatus status);

    Optional<DeliveryJob> findFirstByPurposeAndRecipientOrderByCreatedAtDesc(
            DeliveryPurpose purpose,
            String recipient
    );
}
