package com.notif.scrape.connector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.NormalizedDraft;
import com.notif.scrape.parse.JsonMaps;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CoinGeckoConnector implements SourceConnector {

    private final String url;
    private final JsonMapper jsonMapper;

    public CoinGeckoConnector(
            @Value("${notif.scrape.coingecko-url:https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum&vs_currencies=usd,huf}")
                    String url,
            JsonMapper jsonMapper
    ) {
        this.url = url;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SourceId id() {
        return SourceId.coingecko;
    }

    @Override
    public EventFamily family() {
        return EventFamily.market;
    }

    @Override
    public String locale() {
        return "en";
    }

    @Override
    public String fetchUrl() {
        return url;
    }

    @Override
    public List<NormalizedDraft> normalize(String body) {
        Map<String, Object> root = jsonMapper.readValue(body, new TypeReference<>() {});
        Instant now = Instant.now();
        List<NormalizedDraft> events = new ArrayList<>();
        for (Map.Entry<String, Object> entry : root.entrySet()) {
            Map<String, Object> prices = JsonMaps.map(entry.getValue());
            Double usd = JsonMaps.num(prices.get("usd"));
            Double huf = JsonMaps.num(prices.get("huf"));
            if (usd == null && huf == null) {
                continue;
            }
            String instrument = entry.getKey();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("instrument", instrument);
            payload.put("priceUsd", usd);
            payload.put("priceHuf", huf);
            events.add(new NormalizedDraft(
                    EventFamily.market,
                    SourceId.coingecko,
                    instrument + "@" + now,
                    now,
                    "en",
                    instrument + " " + (usd == null ? huf : usd),
                    "CoinGecko simple price",
                    "https://www.coingecko.com/",
                    payload
            ));
        }
        return events;
    }
}
