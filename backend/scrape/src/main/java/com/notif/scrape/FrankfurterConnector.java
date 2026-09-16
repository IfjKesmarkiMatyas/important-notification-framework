package com.notif.scrape;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FrankfurterConnector implements SourceConnector {

    private final String url;
    private final JsonMapper jsonMapper;

    public FrankfurterConnector(
            @Value("${notif.scrape.frankfurter-url:https://api.frankfurter.app/latest?from=EUR&to=HUF}") String url,
            JsonMapper jsonMapper
    ) {
        this.url = url;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SourceId id() {
        return SourceId.frankfurter;
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
        Map<String, Object> rates = JsonMaps.map(root.get("rates"));
        Double huf = JsonMaps.num(rates.get("HUF"));
        if (huf == null) {
            return List.of();
        }
        String date = JsonMaps.str(root.get("date"));
        Instant occurred = date.isBlank()
                ? Instant.now()
                : LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("instrument", "eurhuf");
        payload.put("priceHuf", huf);
        List<NormalizedDraft> events = new ArrayList<>();
        events.add(new NormalizedDraft(
                EventFamily.market,
                SourceId.frankfurter,
                "eurhuf@" + (date.isBlank() ? occurred : date),
                occurred,
                "en",
                "EUR/HUF " + huf,
                "Frankfurter ECB rate",
                "https://www.frankfurter.app/",
                payload
        ));
        return events;
    }
}
