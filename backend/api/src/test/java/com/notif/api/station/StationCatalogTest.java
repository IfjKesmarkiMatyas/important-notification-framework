package com.notif.api.station;

import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.domain.scrape.SourceId;
import tools.jackson.databind.json.JsonMapper;

class StationCatalogTest {

    @Test
    void packContainsPersonasAndUniqueScrapedEvents() {
        StationPack pack = StationPack.load(new JsonMapper());

        assertThat(pack.password()).isEqualTo("stationstation");
        assertThat(pack.users()).extracting(StationPack.UserSpec::email).containsExactly(
                "ada@notif.local",
                "bela@notif.local",
                "cora@notif.local",
                "denes@notif.local",
                "elena@notif.local"
        );
        assertThat(pack.users()).extracting(StationPack.UserSpec::status).containsExactly(
                UserStatus.ACTIVE,
                UserStatus.ACTIVE,
                UserStatus.ACTIVE,
                UserStatus.INACTIVE,
                UserStatus.ACTIVE
        );
        assertThat(pack.events()).hasSize(13);

        Set<String> keys = pack.events().stream()
                .map(event -> event.event().getSourceId() + "|" + event.event().getExternalId())
                .collect(Collectors.toSet());
        assertThat(keys).hasSize(13);

        assertThat(pack.events())
                .extracting(event -> event.event().getSourceId())
                .contains(SourceId.telex, SourceId.usgs, SourceId.coingecko, SourceId.bbc, SourceId.frankfurter);

        StationPack.UserSpec ada = pack.users().getFirst();
        assertThat(ada.kit().get("interests")).asString().contains("belfold").contains("bitcoin");
        StationPack.UserSpec bela = pack.users().get(1);
        assertThat(bela.kit().toString()).contains("U_BELA");
        StationPack.UserSpec cora = pack.users().get(2);
        assertThat(cora.kit().toString()).contains("minMagnitude=7");
        StationPack.UserSpec elena = pack.users().get(4);
        assertThat(elena.kit().toString()).contains("world");
        assertThat(elena.rulesEn().get("locale")).isEqualTo("en");
    }
}
