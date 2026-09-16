package com.notif.scrape.parse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Topics {

    private static final Pattern TELEX_PATH = Pattern.compile("https?://(?:www\\.)?telex\\.hu/([^/]+)/");

    private Topics() {}

    public static List<String> fromTelex(String link, List<String> categories) {
        Set<String> topics = new LinkedHashSet<>();
        for (String category : categories) {
            String slug = slug(category);
            if (!slug.isBlank()) {
                topics.add(slug);
            }
        }
        Matcher matcher = TELEX_PATH.matcher(link == null ? "" : link);
        if (matcher.find()) {
            String slug = slug(matcher.group(1));
            if (!slug.isBlank() && !slug.matches("\\d+")) {
                topics.add(slug);
            }
        }
        if (topics.isEmpty()) {
            topics.add("belfold");
        }
        return new ArrayList<>(topics);
    }

    public static String slug(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace('ő', 'o')
                .replace('ű', 'u')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return normalized.replace('-', ' ').trim().replace(' ', '_');
    }
}
