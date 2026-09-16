package com.notif.api.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.identity.DefaultsView;
import com.notif.common.dto.identity.KitUpdate;
import com.notif.identity.service.DefaultKitService;

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
    public DefaultsView update(@RequestBody KitUpdate body) {
        return DefaultsView.from(defaults.update(body.kit(), body.rulesHu(), body.rulesEn()));
    }
}
