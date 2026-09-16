package com.notif.api;

import com.notif.delivery.DeliveryChannelType;
import com.notif.delivery.DeliveryService;
import com.notif.identity.InviteService;
import com.notif.identity.UserService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NotifMcpTools {

    private final InviteService invites;
    private final UserService users;
    private final DeliveryService deliveryService;
    private final com.notif.identity.TestDeliveryService testDelivery;

    public NotifMcpTools(
            InviteService invites,
            UserService users,
            DeliveryService deliveryService,
            com.notif.identity.TestDeliveryService testDelivery
    ) {
        this.invites = invites;
        this.users = users;
        this.deliveryService = deliveryService;
        this.testDelivery = testDelivery;
    }

    @McpTool(name = "invite_user", description = "Invite a user by email. Sends an INVITE via the delivery engine.")
    public String inviteUser(
            @McpToolParam(description = "Email address", required = true) String email,
            @McpToolParam(description = "Locale hu or en") String locale
    ) {
        var result = invites.invite(email, locale == null ? "hu" : locale, "MCP");
        return "invited " + result.userId() + " job " + result.deliveryJobId();
    }

    @McpTool(name = "list_users", description = "List all users")
    public String listUsers() {
        return users.list().stream()
                .map(u -> u.getEmail() + " " + u.getStatus() + " " + u.getRole())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }

    @McpTool(name = "update_kit", description = "Replace a user's kit JSON object")
    public String updateKit(
            @McpToolParam(description = "User UUID", required = true) String userId,
            @McpToolParam(description = "Kit JSON as a map", required = true) Map<String, Object> kit
    ) {
        users.updateKit(UUID.fromString(userId), kit, null, null);
        return "updated kit for " + userId;
    }

    @McpTool(name = "resend_invite", description = "Resend invite email with a new token")
    public String resendInvite(
            @McpToolParam(description = "User UUID", required = true) String userId
    ) {
        var result = invites.resend(UUID.fromString(userId), "hu", "MCP");
        return "resent job " + result.deliveryJobId();
    }

    @McpTool(name = "send_test_delivery", description = "Enqueue a TEST delivery job on email, slack, or pushover")
    public String sendTestDelivery(
            @McpToolParam(description = "Channel: email, slack, pushover", required = true) String channel,
            @McpToolParam(description = "Recipient email, Slack id, or Pushover user key") String recipient
    ) {
        UUID id = testDelivery.send(DeliveryChannelType.valueOf(channel), recipient);
        return "queued " + id;
    }

    @McpTool(name = "list_delivery_jobs", description = "List recent delivery jobs")
    public String listDeliveryJobs() {
        return deliveryService.listRecent().stream()
                .map(j -> j.getCreatedAt() + " " + j.getPurpose() + " " + j.getChannel() + " " + j.getStatus()
                        + " " + j.getRecipient())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(none)");
    }
}
