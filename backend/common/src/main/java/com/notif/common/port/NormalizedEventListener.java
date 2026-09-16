package com.notif.common.port;

import com.notif.common.entity.scrape.NormalizedEvent;

public interface NormalizedEventListener {
    void onNormalized(NormalizedEvent event);
}
