package com.notif.delivery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.domain.delivery.DeliveryStatus;
import com.notif.common.entity.delivery.DeliveryJob;

public interface DeliveryJobRepository extends JpaRepository<DeliveryJob, UUID> {
    List<DeliveryJob> findTop100ByOrderByCreatedAtDesc();

    List<DeliveryJob> findByStatusOrderByCreatedAtAsc(DeliveryStatus status);

    Optional<DeliveryJob> findFirstByPurposeAndRecipientOrderByCreatedAtDesc(
            DeliveryPurpose purpose,
            String recipient
    );
}
