package com.notif.api.station;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.dto.decision.DecisionResult;
import com.notif.common.dto.station.StationLoadView;
import com.notif.common.dto.station.StationPreviewView;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.service.DecisionService;
import com.notif.identity.repository.AppUserRepository;
import com.notif.scrape.repository.NormalizedEventRepository;
import tools.jackson.databind.json.JsonMapper;

@Service
public class StationService {

    private final JsonMapper jsonMapper;
    private final AppUserRepository users;
    private final NormalizedEventRepository events;
    private final PasswordEncoder passwords;
    private final DecisionService decisions;

    public StationService(
            JsonMapper jsonMapper,
            AppUserRepository users,
            NormalizedEventRepository events,
            PasswordEncoder passwords,
            DecisionService decisions
    ) {
        this.jsonMapper = jsonMapper;
        this.users = users;
        this.events = events;
        this.passwords = passwords;
        this.decisions = decisions;
    }

    public StationPreviewView preview() {
        return StationPack.load(jsonMapper).preview();
    }

    @Transactional
    public StationLoadView load(boolean decide) {
        StationPack pack = StationPack.load(jsonMapper);
        String hash = passwords.encode(pack.password());
        Instant now = Instant.now();
        List<AppUser> cohort = new ArrayList<>();
        for (StationPack.UserSpec spec : pack.users()) {
            cohort.add(upsertUser(spec, hash, now));
        }
        int inserted = 0;
        int skipped = 0;
        int decisionsRun = 0;
        int fired = 0;
        for (StationPack.EventSpec spec : pack.events()) {
            NormalizedEvent incoming = spec.event();
            Optional<NormalizedEvent> existing = events.findBySourceIdAndExternalId(
                    incoming.getSourceId(),
                    incoming.getExternalId()
            );
            NormalizedEvent persisted;
            if (existing.isPresent()) {
                skipped++;
                persisted = existing.get();
            } else {
                persisted = events.saveAndFlush(incoming);
                inserted++;
            }
            if (decide) {
                List<DecisionResult> results = decisions.evaluate(persisted, cohort);
                decisionsRun += results.size();
                for (DecisionResult result : results) {
                    if (result.outcome() == DecisionOutcome.fire) {
                        fired++;
                    }
                }
            }
        }
        return new StationLoadView(cohort.size(), inserted, skipped, decisionsRun, fired);
    }

    private AppUser upsertUser(StationPack.UserSpec spec, String passwordHash, Instant now) {
        AppUser user = users.findByEmailIgnoreCase(spec.email()).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setId(UUID.nameUUIDFromBytes(spec.email().getBytes(StandardCharsets.UTF_8)));
            created.setEmail(spec.email());
            created.setRole(UserRole.USER);
            created.setCreatedAt(now);
            return created;
        });
        if (user.getRole() == UserRole.ADMIN) {
            return user;
        }
        user.setDisplayName(spec.displayName());
        user.setStatus(spec.status());
        user.setPasswordHash(passwordHash);
        user.setKit(spec.kit());
        user.setRulesHu(spec.rulesHu());
        user.setRulesEn(spec.rulesEn());
        user.setUpdatedAt(now);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        return users.saveAndFlush(user);
    }
}
