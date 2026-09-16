package com.notif.api;

import com.notif.delivery.DeliveryJob;
import com.notif.delivery.DeliveryService;
import com.notif.identity.AppUser;
import com.notif.identity.InviteService;
import com.notif.identity.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

    private final UserService users;
    private final InviteService invites;
    private final DeliveryService deliveryService;

    public AdminUsersController(UserService users, InviteService invites, DeliveryService deliveryService) {
        this.users = users;
        this.invites = invites;
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public List<UserDtos.UserView> list() {
        return users.list().stream().map(this::viewWithInvite).toList();
    }

    @GetMapping("/{id}")
    public UserDtos.UserView get(@PathVariable UUID id) {
        return viewWithInvite(users.require(id));
    }

    @PostMapping("/invite")
    public InviteResponse invite(Authentication authentication, @Valid @RequestBody InviteRequest request) {
        String inviter = inviterName(authentication);
        var result = invites.invite(request.email(), request.locale(), inviter);
        return new InviteResponse(result.userId(), result.deliveryJobId());
    }

    @PostMapping("/{id}/resend-invite")
    public InviteResponse resend(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody(required = false) ResendRequest request
    ) {
        String locale = request == null ? "hu" : request.locale();
        var result = invites.resend(id, locale, inviterName(authentication));
        return new InviteResponse(result.userId(), result.deliveryJobId());
    }

    @PostMapping("/{id}/revoke")
    public UserDtos.UserView revoke(@PathVariable UUID id) {
        invites.revoke(id);
        return viewWithInvite(users.require(id));
    }

    @PostMapping("/{id}/deactivate")
    public UserDtos.UserView deactivate(@PathVariable UUID id) {
        return viewWithInvite(users.deactivate(id));
    }

    @GetMapping("/{id}/kit")
    public UserDtos.KitView kit(@PathVariable UUID id) {
        return UserDtos.kit(users.require(id));
    }

    @PutMapping("/{id}/kit")
    public UserDtos.KitView updateKit(@PathVariable UUID id, @RequestBody UserDtos.KitUpdate body) {
        return UserDtos.kit(users.updateKit(id, body.kit(), body.rulesHu(), body.rulesEn()));
    }

    private UserDtos.UserView viewWithInvite(AppUser user) {
        DeliveryJob job = deliveryService.latestInvite(user.getEmail()).orElse(null);
        if (job == null) {
            return UserDtos.view(user);
        }
        return UserDtos.view(user, job.getStatus().name(), job.getErrorMessage(), job.getCreatedAt());
    }

    private static String inviterName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AppUser user) {
            return user.getDisplayName() == null ? user.getEmail() : user.getDisplayName();
        }
        return "Notif";
    }

    public record InviteRequest(@Email @NotBlank String email, String locale) {}

    public record ResendRequest(String locale) {}

    public record InviteResponse(UUID userId, UUID deliveryJobId) {}
}
