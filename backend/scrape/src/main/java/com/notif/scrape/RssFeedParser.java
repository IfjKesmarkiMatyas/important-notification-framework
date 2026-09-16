package com.notif.scrape;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RssFeedParser {

    private static final DateTimeFormatter RFC_1123 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final DateTimeFormatter RFC_1123_GMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    public record Item(String title, String link, String guid, String description, Instant published, List<String> categories) {}

    private RssFeedParser() {}

    public static List<Item> parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList nodes = doc.getElementsByTagName("item");
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element item = (Element) nodes.item(i);
                String title = text(item, "title");
                String link = text(item, "link");
                String guid = text(item, "guid");
                String description = text(item, "description");
                Instant published = parseDate(text(item, "pubDate"));
                items.add(new Item(title, link, guid.isBlank() ? link : guid, description, published, categories(item)));
            }
            return items;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot parse RSS: " + ex.getMessage(), ex);
        }
    }

    private static List<String> categories(Element item) {
        NodeList nodes = item.getElementsByTagName("category");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            String value = nodes.item(i).getTextContent();
            if (value != null && !value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    private static String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent() == null) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    static Instant parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return ZonedDateTime.parse(raw, RFC_1123).toInstant();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return ZonedDateTime.parse(raw, RFC_1123_GMT).toInstant();
        } catch (DateTimeParseException ignored) {
            return Instant.now();
        }
    }
}
