package com.notif.api;

import com.notif.identity.DefaultKit;
import com.notif.identity.DefaultKitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/defaults")
public class AdminDefaultsController {

    private final DefaultKitService defaults;

    public AdminDefaultsController(DefaultKitService defaults) {
        this.defaults = defaults;
    }

    @GetMapping
    public DefaultsView get() {
        return DefaultsView.from(defaults.get());
    }

    @PutMapping
    public DefaultsView update(@RequestBody UserDtos.KitUpdate body) {
        return DefaultsView.from(defaults.update(body.kit(), body.rulesHu(), body.rulesEn()));
    }

    public record DefaultsView(
            Map<String, Object> kit,
            Map<String, Object> rulesHu,
            Map<String, Object> rulesEn,
            Instant updatedAt
    ) {
        static DefaultsView from(DefaultKit row) {
            return new DefaultsView(row.getKit(), row.getRulesHu(), row.getRulesEn(), row.getUpdatedAt());
        }
    }
}
