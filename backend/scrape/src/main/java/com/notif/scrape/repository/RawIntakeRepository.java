package com.notif.scrape.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.scrape.RawIntake;

public interface RawIntakeRepository extends JpaRepository<RawIntake, UUID> {

    Optional<RawIntake> findById(UUID id);
}
