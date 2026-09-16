package com.notif.identity;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DefaultKitService {

    private final DefaultKitRepository repository;
    private final JsonMapper jsonMapper;

    public DefaultKitService(DefaultKitRepository repository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public DefaultKit ensureSeeded() {
        return repository.findById((short) 1).orElseGet(() -> {
            DefaultKit row = new DefaultKit();
            row.setId((short) 1);
            row.setKit(readJson("defaults/default-kit.json"));
            row.setRulesHu(readJson("defaults/default-rules.hu.json"));
            row.setRulesEn(readJson("defaults/default-rules.en.json"));
            row.setUpdatedAt(Instant.now());
            return repository.save(row);
        });
    }

    public DefaultKit get() {
        return repository.findById((short) 1).orElseGet(this::ensureSeeded);
    }

    @Transactional
    public DefaultKit update(Map<String, Object> kit, Map<String, Object> rulesHu, Map<String, Object> rulesEn) {
        DefaultKit row = get();
        if (kit != null) {
            row.setKit(kit);
        }
        if (rulesHu != null) {
            row.setRulesHu(rulesHu);
        }
        if (rulesEn != null) {
            row.setRulesEn(rulesEn);
        }
        row.setUpdatedAt(Instant.now());
        return repository.save(row);
    }

    public Map<String, Object> copyKitFor(String email) {
        Map<String, Object> copy = deepCopy(get().getKit());
        Object prefs = copy.get("preferences");
        if (prefs instanceof Map<?, ?> raw) {
            @SuppressWarnings("unchecked")
            Map<String, Object> preferences = (Map<String, Object>) raw;
            if (preferences.get("email") == null || String.valueOf(preferences.get("email")).isBlank()) {
                preferences.put("email", email);
            }
        }
        return copy;
    }

    public Map<String, Object> copyRulesHu() {
        return deepCopy(get().getRulesHu());
    }

    public Map<String, Object> copyRulesEn() {
        return deepCopy(get().getRulesEn());
    }

    private Map<String, Object> readJson(String path) {
        try {
            return jsonMapper.readValue(new ClassPathResource(path).getInputStream(), new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IdentityException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot read " + path);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> source) {
        return jsonMapper.convertValue(source, LinkedHashMap.class);
    }
}
