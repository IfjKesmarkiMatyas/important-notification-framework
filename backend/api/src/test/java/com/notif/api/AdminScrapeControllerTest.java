package com.notif.api;

import com.notif.scrape.EventFamily;
import com.notif.scrape.NormalizedEvent;
import com.notif.scrape.RawIntake;
import com.notif.scrape.ScrapeRun;
import com.notif.scrape.ScrapeRunStatus;
import com.notif.scrape.ScrapeService;
import com.notif.scrape.SourceId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminScrapeControllerTest {

    @Mock
    private ScrapeService scrapeService;

    @InjectMocks
    private AdminScrapeController controller;

    @Test
    void runAllDelegatesToService() {
        ScrapeRun run = new ScrapeRun();
        run.setId(UUID.randomUUID());
        run.setSourceId(SourceId.telex);
        run.setStatus(ScrapeRunStatus.ok);
        run.setFetched(1);
        run.setNormalized(1);
        run.setStartedAt(Instant.parse("2026-09-16T12:00:00Z"));
        when(scrapeService.runAll()).thenReturn(List.of(run));

        List<AdminScrapeController.RunView> views = controller.run(new AdminScrapeController.RunRequest("all"));

        assertThat(views).hasSize(1);
        assertThat(views.getFirst().sourceId()).isEqualTo("telex");
        verify(scrapeService).runAll();
    }

    @Test
    void eventDetailIncludesRawExcerpt() {
        UUID eventId = UUID.randomUUID();
        UUID intakeId = UUID.randomUUID();
        NormalizedEvent event = new NormalizedEvent();
        event.setId(eventId);
        event.setFamily(EventFamily.breaking);
        event.setSourceId(SourceId.telex);
        event.setExternalId("guid");
        event.setOccurredAt(Instant.parse("2026-09-16T12:00:00Z"));
        event.setIngestedAt(Instant.parse("2026-09-16T12:01:00Z"));
        event.setLocale("hu");
        event.setHeadline("Hír");
        event.setSummary("Kivonat");
        event.setCanonicalUrl("https://telex.hu/x");
        event.setPayload(Map.of("topics", List.of("belfold")));
        event.setRawIntakeId(intakeId);
        RawIntake intake = new RawIntake();
        intake.setBody("<rss>raw body</rss>");
        when(scrapeService.requireEvent(eventId)).thenReturn(event);
        when(scrapeService.intake(intakeId)).thenReturn(Optional.of(intake));

        AdminScrapeController.EventDetailView detail = controller.event(eventId);

        assertThat(detail.headline()).isEqualTo("Hír");
        assertThat(detail.rawExcerpt()).contains("raw body");
    }
}
