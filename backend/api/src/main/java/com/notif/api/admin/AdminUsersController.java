package com.notif.api.admin;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.notif.common.dto.identity.InviteRequest;
import com.notif.common.dto.identity.InviteResponse;
import com.notif.common.dto.identity.KitUpdate;
import com.notif.common.dto.identity.KitView;
import com.notif.common.dto.identity.ResendRequest;
import com.notif.common.dto.identity.UserView;
import com.notif.common.entity.delivery.DeliveryJob;
import com.notif.common.entity.identity.AppUser;
import com.notif.delivery.service.DeliveryService;
import com.notif.identity.service.InviteService;
import com.notif.identity.service.UserService;

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
    public List<UserView> list() {
        return users.list().stream().map(this::viewWithInvite).toList();
    }

    @GetMapping("/{id}")
    public UserView get(@PathVariable UUID id) {
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
    public UserView revoke(@PathVariable UUID id) {
        invites.revoke(id);
        return viewWithInvite(users.require(id));
    }

    @PostMapping("/{id}/deactivate")
    public UserView deactivate(@PathVariable UUID id) {
        return viewWithInvite(users.deactivate(id));
    }

    @GetMapping("/{id}/kit")
    public KitView kit(@PathVariable UUID id) {
        return KitView.from(users.require(id));
    }

    @PutMapping("/{id}/kit")
    public KitView updateKit(@PathVariable UUID id, @RequestBody KitUpdate body) {
        return KitView.from(users.updateKit(id, body.kit(), body.rulesHu(), body.rulesEn()));
    }

    private UserView viewWithInvite(AppUser user) {
        DeliveryJob job = deliveryService.latestInvite(user.getEmail()).orElse(null);
        if (job == null) {
            return UserView.from(user);
        }
        return UserView.from(user, job.getStatus().name(), job.getErrorMessage(), job.getCreatedAt());
    }

    private static String inviterName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AppUser user) {
            return user.getDisplayName() == null ? user.getEmail() : user.getDisplayName();
        }
        return "Notif";
    }
}
