package com.notif.scrape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.scrape.MarketSnapshot;

public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshot, String> {}
