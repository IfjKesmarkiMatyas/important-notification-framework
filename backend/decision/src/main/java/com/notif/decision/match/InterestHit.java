package com.notif.decision.match;

import java.util.List;

public record InterestHit(
        String type,
        String whenKey,
        String kind,
        String instrument,
        Double eventValue,
        Double threshold,
        List<String> matchedTopics
) {}
