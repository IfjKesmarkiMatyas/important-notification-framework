package com.notif.common.dto.station;

import java.util.List;

public record StationPreviewView(
        int version,
        List<User> users,
        List<Event> events
) {
    public record User(String email, String displayName, String status) {}

    public record Event(String caseId, String family, String sourceId, String headline) {}
}
