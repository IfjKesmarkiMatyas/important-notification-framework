package com.notif.api;

import com.notif.identity.AppUser;
import com.notif.identity.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService users;

    public MeController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public UserDtos.UserView me(Authentication authentication) {
        return UserDtos.view(current(authentication));
    }

    @GetMapping("/kit")
    public UserDtos.KitView kit(Authentication authentication) {
        AppUser user = current(authentication);
        return UserDtos.kit(user);
    }

    @PutMapping("/kit")
    public UserDtos.KitView updateKit(Authentication authentication, @RequestBody UserDtos.KitUpdate body) {
        AppUser user = current(authentication);
        AppUser saved = users.updateKit(user.getId(), body.kit(), body.rulesHu(), body.rulesEn());
        return UserDtos.kit(saved);
    }

    private static AppUser current(Authentication authentication) {
        return (AppUser) authentication.getPrincipal();
    }
}
