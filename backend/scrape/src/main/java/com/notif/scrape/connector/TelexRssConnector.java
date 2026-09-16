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
import com.notif.scrape.parse.RssFeedParser;
import com.notif.scrape.parse.Topics;

@Component
public class TelexRssConnector implements SourceConnector {

    private final String url;

    public TelexRssConnector(@Value("${notif.scrape.telex-url:https://telex.hu/rss}") String url) {
        this.url = url;
    }

    @Override
    public SourceId id() {
        return SourceId.telex;
    }

    @Override
    public EventFamily family() {
        return EventFamily.breaking;
    }

    @Override
    public String locale() {
        return "hu";
    }

    @Override
    public String fetchUrl() {
        return url;
    }

    @Override
    public List<NormalizedDraft> normalize(String body) {
        List<NormalizedDraft> events = new ArrayList<>();
        for (RssFeedParser.Item item : RssFeedParser.parse(body)) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("topics", Topics.fromTelex(item.link(), item.categories()));
            Instant occurred = item.published() == null ? Instant.now() : item.published();
            String headline = item.title().isBlank() ? "(untitled)" : item.title();
            events.add(new NormalizedDraft(
                    EventFamily.breaking,
                    SourceId.telex,
                    item.guid().isBlank() ? headline + "@" + occurred : item.guid(),
                    occurred,
                    "hu",
                    headline,
                    stripHtml(item.description()),
                    item.link(),
                    payload
            ));
        }
        return events;
    }

    static String stripHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replaceAll("(?s)<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }
}
