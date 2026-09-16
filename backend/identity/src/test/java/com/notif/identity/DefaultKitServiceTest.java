package com.notif.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultKitServiceTest {

    @Mock
    private DefaultKitRepository repository;

    private DefaultKitService service;

    @BeforeEach
    void setUp() {
        service = new DefaultKitService(repository, new JsonMapper());
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void ensureSeededLoadsClasspathDefaults() {
        when(repository.findById((short) 1)).thenReturn(Optional.empty());

        DefaultKit seeded = service.ensureSeeded();

        assertThat(seeded.getKit()).containsKey("preferences");
        assertThat(seeded.getRulesHu()).containsEntry("locale", "hu");
        assertThat(seeded.getRulesEn()).containsEntry("locale", "en");
    }

    @Test
    void copyKitForFillsBlankEmailWithoutMutatingSource() {
        DefaultKit row = new DefaultKit();
        Map<String, Object> preferences = new LinkedHashMap<>();
        preferences.put("email", "");
        preferences.put("pushoverUserKey", "");
        Map<String, Object> kit = new LinkedHashMap<>();
        kit.put("preferences", preferences);
        row.setKit(kit);
        row.setRulesHu(Map.of("locale", "hu"));
        row.setRulesEn(Map.of("locale", "en"));
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        Map<String, Object> copy = service.copyKitFor("ada@notif.local");

        @SuppressWarnings("unchecked")
        Map<String, Object> copiedPrefs = (Map<String, Object>) copy.get("preferences");
        assertThat(copiedPrefs.get("email")).isEqualTo("ada@notif.local");
        assertThat(preferences.get("email")).isEqualTo("");
        assertThat(service.copyRulesHu()).containsEntry("locale", "hu");
        assertThat(service.copyRulesEn()).containsEntry("locale", "en");
    }

    @Test
    void updateReplacesProvidedDocuments() {
        DefaultKit row = new DefaultKit();
        row.setKit(Map.of("version", 1));
        row.setRulesHu(Map.of("locale", "hu"));
        row.setRulesEn(Map.of("locale", "en"));
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        DefaultKit saved = service.update(Map.of("version", 2), null, Map.of("locale", "en-GB"));

        assertThat(saved.getKit()).containsEntry("version", 2);
        assertThat(saved.getRulesHu()).containsEntry("locale", "hu");
        assertThat(saved.getRulesEn()).containsEntry("locale", "en-GB");
    }
}
