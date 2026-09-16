package com.notif.api.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.identity.AcceptRequest;
import com.notif.common.dto.identity.InvitePeekResponse;
import com.notif.common.dto.identity.LoginRequest;
import com.notif.common.dto.identity.TokenResponse;
import com.notif.common.entity.identity.AppUser;
import com.notif.identity.service.InviteService;
import com.notif.identity.service.JwtService;
import com.notif.identity.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final InviteService invites;
    private final JwtService jwt;

    public AuthController(UserService users, InviteService invites, JwtService jwt) {
        this.users = users;
        this.invites = invites;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        AppUser user = users.authenticate(request.email(), request.password());
        return toToken(user);
    }

    @GetMapping("/invite/{token}")
    public InvitePeekResponse peek(@PathVariable String token) {
        return new InvitePeekResponse(invites.peek(token).email());
    }

    @PostMapping("/invite/{token}/accept")
    public TokenResponse accept(@PathVariable String token, @Valid @RequestBody AcceptRequest request) {
        AppUser user = invites.accept(token, request.displayName(), request.password());
        return toToken(user);
    }

    private TokenResponse toToken(AppUser user) {
        return new TokenResponse(
                jwt.issue(user),
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
