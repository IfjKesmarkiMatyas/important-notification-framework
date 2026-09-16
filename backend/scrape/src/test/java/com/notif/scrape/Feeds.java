package com.notif.scrape;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class Feeds {

    private Feeds() {}

    static String load(String name) {
        String path = name.startsWith("/") ? name : "/feeds/" + name;
        try (InputStream in = Feeds.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
