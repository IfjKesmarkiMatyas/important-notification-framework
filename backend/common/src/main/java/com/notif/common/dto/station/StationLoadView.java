package com.notif.common.dto.station;

public record StationLoadView(
        int usersUpserted,
        int eventsInserted,
        int eventsSkipped,
        int decisionsRun,
        int fired
) {}
