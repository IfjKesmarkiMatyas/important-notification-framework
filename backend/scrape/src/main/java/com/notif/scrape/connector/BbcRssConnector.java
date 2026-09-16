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
public class BbcRssConnector implements SourceConnector {

    private final String url;

    public BbcRssConnector(
            @Value("${notif.scrape.bbc-url:https://feeds.bbci.co.uk/news/world/rss.xml}") String url
    ) {
        this.url = url;
    }

    @Override
    public SourceId id() {
        return SourceId.bbc;
    }

    @Override
    public EventFamily family() {
        return EventFamily.breaking;
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
        List<NormalizedDraft> events = new ArrayList<>();
        for (RssFeedParser.Item item : RssFeedParser.parse(body)) {
            List<String> topics = new ArrayList<>();
            topics.add("world");
            for (String category : item.categories()) {
                String slug = Topics.slug(category);
                if (!slug.isBlank() && !topics.contains(slug)) {
                    topics.add(slug);
                }
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("topics", topics);
            Instant occurred = item.published() == null ? Instant.now() : item.published();
            String headline = item.title().isBlank() ? "(untitled)" : item.title();
            events.add(new NormalizedDraft(
                    EventFamily.breaking,
                    SourceId.bbc,
                    item.guid().isBlank() ? headline + "@" + occurred : item.guid(),
                    occurred,
                    "en",
                    headline,
                    TelexRssConnector.stripHtml(item.description()),
                    item.link(),
                    payload
            ));
        }
        return events;
    }
}
