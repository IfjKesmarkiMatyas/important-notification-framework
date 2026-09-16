package com.notif.scrape;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "market_snapshots")
public class MarketSnapshot {

    @Id
    @Column(length = 80)
    private String instrument;

    @Column(name = "price_usd")
    private Double priceUsd;

    @Column(name = "price_huf")
    private Double priceHuf;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Double getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(Double priceUsd) {
        this.priceUsd = priceUsd;
    }

    public Double getPriceHuf() {
        return priceHuf;
    }

    public void setPriceHuf(Double priceHuf) {
        this.priceHuf = priceHuf;
    }

    public Instant getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(Instant seenAt) {
        this.seenAt = seenAt;
    }
}
