package com.notif.api.me;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.identity.KitUpdate;
import com.notif.common.dto.identity.KitView;
import com.notif.common.dto.identity.UserView;
import com.notif.common.entity.identity.AppUser;
import com.notif.identity.service.UserService;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService users;

    public MeController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public UserView me(Authentication authentication) {
        return UserView.from(current(authentication));
    }

    @GetMapping("/kit")
    public KitView kit(Authentication authentication) {
        AppUser user = current(authentication);
        return KitView.from(user);
    }

    @PutMapping("/kit")
    public KitView updateKit(Authentication authentication, @RequestBody KitUpdate body) {
        AppUser user = current(authentication);
        AppUser saved = users.updateKit(user.getId(), body.kit(), body.rulesHu(), body.rulesEn());
        return KitView.from(saved);
    }

    private static AppUser current(Authentication authentication) {
        return (AppUser) authentication.getPrincipal();
    }
}
