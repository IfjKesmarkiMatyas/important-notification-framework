package com.notif.scrape;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScrapeScheduler {

    private final ScrapeService scrapeService;

    public ScrapeScheduler(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @Scheduled(cron = "${notif.scrape.cron:0 */15 * * * *}")
    public void tick() {
        scrapeService.runAll();
    }
}
