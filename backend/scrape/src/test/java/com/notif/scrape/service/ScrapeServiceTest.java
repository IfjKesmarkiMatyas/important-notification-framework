package com.notif.scrape.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.ScrapeRunStatus;
import com.notif.common.domain.scrape.SourceHealthStatus;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.FetchResult;
import com.notif.common.entity.scrape.MarketSnapshot;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.common.entity.scrape.ScrapeRun;
import com.notif.scrape.client.ScrapeClient;
import com.notif.scrape.connector.BbcRssConnector;
import com.notif.scrape.connector.CoinGeckoConnector;
import com.notif.scrape.connector.FrankfurterConnector;
import com.notif.scrape.connector.TelexRssConnector;
import com.notif.scrape.connector.UsgsGeoJsonConnector;
import com.notif.scrape.repository.MarketSnapshotRepository;
import com.notif.scrape.repository.NormalizedEventRepository;
import com.notif.scrape.repository.RawIntakeRepository;
import com.notif.scrape.repository.ScrapeRunRepository;
import com.notif.scrape.support.Feeds;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScrapeServiceTest {

    @Mock
    private ScrapeClient client;

    @Mock
    private ScrapeRunRepository runs;

    @Mock
    private RawIntakeRepository intakes;

    @Mock
    private NormalizedEventRepository events;

    @Mock
    private MarketSnapshotRepository snapshots;

    private ScrapeService service;

    @BeforeEach
    void setUp() {
        JsonMapper json = new JsonMapper();
        service = new ScrapeService(client, runs, intakes, events, snapshots, List.of(
                new TelexRssConnector("https://telex.hu/rss"),
                new BbcRssConnector("https://feeds.bbci.co.uk/news/world/rss.xml"),
                new UsgsGeoJsonConnector("https://example.test/usgs", json),
                new CoinGeckoConnector("https://example.test/gecko", json),
                new FrankfurterConnector("https://example.test/fx", json)
        ), List.of());
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(intakes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void telexRunPersistsNormalizedBreakingEvent() {
        when(client.get("https://telex.hu/rss")).thenReturn(FetchResult.ok(200, "application/rss+xml", Feeds.load("telex.rss.xml")));
        when(events.existsBySourceIdAndExternalId(any(), any())).thenReturn(false);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScrapeRun run = service.run(SourceId.telex);

        assertThat(run.getStatus()).isEqualTo(ScrapeRunStatus.ok);
        assertThat(run.getFetched()).isEqualTo(1);
        assertThat(run.getNormalized()).isEqualTo(1);
        ArgumentCaptor<NormalizedEvent> captor = ArgumentCaptor.forClass(NormalizedEvent.class);
        verify(events).save(captor.capture());
        assertThat(captor.getValue().getFamily()).isEqualTo(EventFamily.breaking);
        assertThat(captor.getValue().getLocale()).isEqualTo("hu");
        assertThat(captor.getValue().getExternalId()).isEqualTo("https://telex.hu/belfold/2026/09/16/foldrenges");
    }

    @Test
    void duplicateGuidIsSkipped() {
        when(client.get("https://telex.hu/rss")).thenReturn(FetchResult.ok(200, "application/rss+xml", Feeds.load("telex.rss.xml")));
        when(events.existsBySourceIdAndExternalId(SourceId.telex, "https://telex.hu/belfold/2026/09/16/foldrenges"))
                .thenReturn(true);

        ScrapeRun run = service.run(SourceId.telex);

        assertThat(run.getStatus()).isEqualTo(ScrapeRunStatus.empty);
        assertThat(run.getNormalized()).isEqualTo(0);
        verify(events, never()).save(any());
    }

    @Test
    void usgsHttpErrorDoesNotStopTelex() {
        when(client.get("https://telex.hu/rss")).thenReturn(FetchResult.ok(200, "application/rss+xml", Feeds.load("telex.rss.xml")));
        when(client.get("https://feeds.bbci.co.uk/news/world/rss.xml")).thenReturn(FetchResult.fail(500, "", "bbc down"));
        when(client.get("https://example.test/usgs")).thenReturn(FetchResult.fail(500, "", "usgs down"));
        when(client.get("https://example.test/gecko")).thenReturn(FetchResult.fail(500, "", "gecko down"));
        when(client.get("https://example.test/fx")).thenReturn(FetchResult.fail(500, "", "fx down"));
        when(events.existsBySourceIdAndExternalId(any(), any())).thenReturn(false);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ScrapeRun> result = service.runAll();

        assertThat(result).hasSize(5);
        assertThat(result.stream().filter(run -> run.getSourceId() == SourceId.telex).findFirst().orElseThrow().getStatus())
                .isEqualTo(ScrapeRunStatus.ok);
        assertThat(result.stream().filter(run -> run.getSourceId() == SourceId.usgs).findFirst().orElseThrow().getStatus())
                .isEqualTo(ScrapeRunStatus.error);
    }

    @Test
    void firstMarketCycleStoresSnapshotWithoutEventThenComputesMovePercent() {
        when(client.get("https://example.test/gecko")).thenReturn(FetchResult.ok(200, "application/json", Feeds.load("coingecko.json")));
        when(snapshots.findById("bitcoin")).thenReturn(Optional.empty());
        when(snapshots.findById("ethereum")).thenReturn(Optional.empty());
        when(snapshots.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScrapeRun first = service.run(SourceId.coingecko);
        assertThat(first.getNormalized()).isEqualTo(0);
        assertThat(first.getStatus()).isEqualTo(ScrapeRunStatus.empty);
        verify(events, never()).save(any());
        verify(snapshots, times(2)).save(any());

        MarketSnapshot previous = new MarketSnapshot();
        previous.setInstrument("bitcoin");
        previous.setPriceUsd(100000.0);
        previous.setPriceHuf(36000000.0);
        when(snapshots.findById("bitcoin")).thenReturn(Optional.of(previous));
        MarketSnapshot eth = new MarketSnapshot();
        eth.setInstrument("ethereum");
        eth.setPriceUsd(4000.0);
        when(snapshots.findById("ethereum")).thenReturn(Optional.of(eth));
        when(events.existsBySourceIdAndExternalId(any(), any())).thenReturn(false);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScrapeRun second = service.run(SourceId.coingecko);
        assertThat(second.getStatus()).isEqualTo(ScrapeRunStatus.ok);
        ArgumentCaptor<NormalizedEvent> captor = ArgumentCaptor.forClass(NormalizedEvent.class);
        verify(events, times(2)).save(captor.capture());
        NormalizedEvent bitcoin = captor.getAllValues().stream()
                .filter(event -> "bitcoin".equals(event.getPayload().get("instrument")))
                .findFirst()
                .orElseThrow();
        assertThat(bitcoin.getPayload()).containsEntry("movePercent", 8.0).containsEntry("previousUsd", 100000.0);
    }

    @Test
    void healthIsSilentAfterThirtyMinutesWithoutSuccess() {
        ScrapeRun last = new ScrapeRun();
        last.setStatus(ScrapeRunStatus.ok);
        last.setFinishedAt(Instant.parse("2026-09-16T11:00:00Z"));
        Instant now = Instant.parse("2026-09-16T11:31:00Z");
        assertThat(ScrapeService.health(Optional.of(last), Optional.of(last), now)).isEqualTo(SourceHealthStatus.silent);
    }

    @Test
    void healthIsErrorOnLastFailedRun() {
        ScrapeRun last = new ScrapeRun();
        last.setStatus(ScrapeRunStatus.error);
        last.setFinishedAt(Instant.parse("2026-09-16T12:00:00Z"));
        assertThat(ScrapeService.health(Optional.of(last), Optional.empty(), Instant.parse("2026-09-16T12:01:00Z")))
                .isEqualTo(SourceHealthStatus.error);
    }

    @Test
    void exportMapsDecisionContractFields() {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        event.setFamily(EventFamily.disaster);
        event.setSourceId(SourceId.usgs);
        event.setExternalId("us7000test64");
        event.setOccurredAt(Instant.parse("2026-09-16T12:00:00Z"));
        event.setIngestedAt(Instant.parse("2026-09-16T12:01:00Z"));
        event.setLocale("en");
        event.setHeadline("6.4 — near Tokyo, Japan");
        event.setSummary("near Tokyo, Japan");
        event.setCanonicalUrl("https://earthquake.usgs.gov/earthquakes/eventpage/us7000test64");
        event.setPayload(java.util.Map.of("kind", "earthquake", "magnitude", 6.4));
        event.setRawIntakeId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));

        assertThat(service.toExport(event))
                .containsEntry("family", "disaster")
                .containsEntry("sourceId", "usgs")
                .containsEntry("externalId", "us7000test64")
                .containsKey("payload")
                .containsKey("rawIntakeId");
    }
}
