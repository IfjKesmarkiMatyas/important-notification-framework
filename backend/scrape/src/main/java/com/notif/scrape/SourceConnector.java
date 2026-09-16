package com.notif.scrape;

import java.util.List;

public interface SourceConnector {

    SourceId id();

    EventFamily family();

    String locale();

    String fetchUrl();

    List<NormalizedDraft> normalize(String body);
}
