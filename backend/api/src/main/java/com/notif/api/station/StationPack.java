package com.notif.api.station;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.domain.scrape.EventFamily;
import com.notif.common.domain.scrape.SourceId;
import com.notif.common.dto.station.StationPreviewView;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.common.exception.IdentityException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public final class StationPack {

    public static final String RESOURCE = "station/station.json";

    private final int version;
    private final String password;
    private final List<UserSpec> users;
    private final List<EventSpec> events;

    private StationPack(int version, String password, List<UserSpec> users, List<EventSpec> events) {
        this.version = version;
        this.password = password;
        this.users = users;
        this.events = events;
    }

    public static StationPack load(JsonMapper jsonMapper) {
        try {
            Map<String, Object> raw = jsonMapper.readValue(
                    new ClassPathResource(RESOURCE).getInputStream(),
                    new TypeReference<>() {}
            );
            Object versionRaw = raw.get("version");
            int version = versionRaw instanceof Number number ? number.intValue() : 1;
            String password = String.valueOf(raw.get("password"));
            List<UserSpec> users = new ArrayList<>();
            if (raw.get("users") instanceof List<?> rows) {
                for (Object row : rows) {
                    if (row instanceof Map<?, ?> map) {
                        users.add(userSpec(map));
                    }
                }
            }
            List<EventSpec> events = new ArrayList<>();
            if (raw.get("events") instanceof List<?> rows) {
                for (Object row : rows) {
                    if (row instanceof Map<?, ?> map) {
                        events.add(eventSpec(map));
                    }
                }
            }
            return new StationPack(version, password, List.copyOf(users), List.copyOf(events));
        } catch (IdentityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IdentityException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot load " + RESOURCE);
        }
    }

    public int version() {
        return version;
    }

    public String password() {
        return password;
    }

    public List<UserSpec> users() {
        return users;
    }

    public List<EventSpec> events() {
        return events;
    }

    public StationPreviewView preview() {
        return new StationPreviewView(
                version,
                users.stream()
                        .map(user -> new StationPreviewView.User(user.email(), user.displayName(), user.status().name()))
                        .toList(),
                events.stream()
                        .map(event -> new StationPreviewView.Event(
                                event.caseId(),
                                event.event().getFamily().name(),
                                event.event().getSourceId().name(),
                                event.event().getHeadline()
                        ))
                        .toList()
        );
    }

    @SuppressWarnings("unchecked")
    private static UserSpec userSpec(Map<?, ?> raw) {
        Map<String, Object> kit = raw.get("kit") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        Map<String, Object> rulesHu = raw.get("rulesHu") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        Map<String, Object> rulesEn = raw.get("rulesEn") instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        return new UserSpec(
                String.valueOf(raw.get("email")).trim().toLowerCase(),
                String.valueOf(raw.get("displayName")),
                UserStatus.valueOf(String.valueOf(raw.get("status"))),
                kit,
                rulesHu,
                rulesEn
        );
    }

    @SuppressWarnings("unchecked")
    private static EventSpec eventSpec(Map<?, ?> raw) {
        NormalizedEvent event = new NormalizedEvent();
        event.setId(UUID.fromString(String.valueOf(raw.get("id"))));
        event.setFamily(EventFamily.valueOf(String.valueOf(raw.get("family"))));
        event.setSourceId(SourceId.valueOf(String.valueOf(raw.get("sourceId"))));
        event.setExternalId(String.valueOf(raw.get("externalId")));
        event.setOccurredAt(OffsetDateTime.parse(String.valueOf(raw.get("occurredAt"))).toInstant());
        event.setIngestedAt(event.getOccurredAt());
        event.setLocale(String.valueOf(raw.get("locale")));
        event.setHeadline(String.valueOf(raw.get("headline")));
        event.setSummary(raw.get("summary") == null ? null : String.valueOf(raw.get("summary")));
        event.setCanonicalUrl(raw.get("canonicalUrl") == null ? null : String.valueOf(raw.get("canonicalUrl")));
        Map<String, Object> payload = raw.get("payload") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        event.setPayload(payload);
        return new EventSpec(String.valueOf(raw.get("caseId")), event);
    }

    public record UserSpec(
            String email,
            String displayName,
            UserStatus status,
            Map<String, Object> kit,
            Map<String, Object> rulesHu,
            Map<String, Object> rulesEn
    ) {}

    public record EventSpec(String caseId, NormalizedEvent event) {}
}
