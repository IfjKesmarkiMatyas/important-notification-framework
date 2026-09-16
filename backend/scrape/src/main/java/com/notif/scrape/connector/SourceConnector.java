package com.notif.scrape.connector;

import java.util.List;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.scrape.NormalizedDraft;

public interface SourceConnector {

    SourceId id();

    EventFamily family();

    String locale();

    String fetchUrl();

    List<NormalizedDraft> normalize(String body);
}
