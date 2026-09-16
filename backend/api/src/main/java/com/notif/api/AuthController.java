package com.notif.api;

import com.notif.identity.AppUser;
import com.notif.identity.JwtService;
import com.notif.identity.InviteService;
import com.notif.identity.UserRole;
import com.notif.identity.UserService;
import com.notif.identity.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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

    public record InvitePeekResponse(String email) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record AcceptRequest(
            @NotBlank @Size(min = 1, max = 200) String displayName,
            @NotBlank @Size(min = 8, max = 200) String password
    ) {}

    public record TokenResponse(
            String token,
            UUID id,
            String email,
            String displayName,
            UserRole role,
            UserStatus status
    ) {}
}
