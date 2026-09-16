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
public class UsgsGeoJsonConnector implements SourceConnector {

    private final String url;
    private final JsonMapper jsonMapper;

    public UsgsGeoJsonConnector(
            @Value("${notif.scrape.usgs-url:https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson}")
                    String url,
            JsonMapper jsonMapper
    ) {
        this.url = url;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SourceId id() {
        return SourceId.usgs;
    }

    @Override
    public EventFamily family() {
        return EventFamily.disaster;
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
        List<NormalizedDraft> events = new ArrayList<>();
        for (Object feature : JsonMaps.list(root.get("features"))) {
            Map<String, Object> item = JsonMaps.map(feature);
            Map<String, Object> properties = JsonMaps.map(item.get("properties"));
            Map<String, Object> geometry = JsonMaps.map(item.get("geometry"));
            List<?> coordinates = JsonMaps.list(geometry.get("coordinates"));
            Double mag = JsonMaps.num(properties.get("mag"));
            String place = JsonMaps.str(properties.get("place"));
            String url = JsonMaps.str(properties.get("url"));
            Double timeMs = JsonMaps.num(properties.get("time"));
            Instant occurred = timeMs == null ? Instant.now() : Instant.ofEpochMilli(timeMs.longValue());
            String externalId = JsonMaps.str(item.get("id"));
            if (externalId.isBlank()) {
                externalId = place + "@" + occurred;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("kind", "earthquake");
            payload.put("magnitude", mag);
            payload.put("place", place);
            if (coordinates.size() >= 2) {
                payload.put("lon", JsonMaps.num(coordinates.get(0)));
                payload.put("lat", JsonMaps.num(coordinates.get(1)));
            }
            String headline = (mag == null ? "?" : mag) + " — " + (place.isBlank() ? "earthquake" : place);
            events.add(new NormalizedDraft(
                    EventFamily.disaster,
                    SourceId.usgs,
                    externalId,
                    occurred,
                    "en",
                    headline,
                    place,
                    url,
                    payload
            ));
        }
        return events;
    }
}
