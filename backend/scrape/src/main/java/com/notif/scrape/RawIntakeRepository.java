package com.notif.scrape;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RawIntakeRepository extends JpaRepository<RawIntake, UUID> {

    Optional<RawIntake> findById(UUID id);
}
