package com.notif.api;

import com.notif.delivery.DeliveryChannelType;
import com.notif.delivery.DeliveryJob;
import com.notif.delivery.DeliveryPurpose;
import com.notif.delivery.DeliveryService;
import com.notif.delivery.DeliveryStatus;
import com.notif.identity.AppUser;
import com.notif.identity.InviteService;
import com.notif.identity.TestDeliveryService;
import com.notif.identity.UserRole;
import com.notif.identity.UserService;
import com.notif.identity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifMcpToolsTest {

    @Mock
    private InviteService invites;

    @Mock
    private UserService users;

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private TestDeliveryService testDelivery;

    @InjectMocks
    private NotifMcpTools tools;

    @Test
    void inviteUserDefaultsLocaleAndReportsIds() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(invites.invite("ada@notif.local", "hu", "MCP"))
                .thenReturn(new InviteService.InviteResult(userId, jobId));

        assertThat(tools.inviteUser("ada@notif.local", null))
                .isEqualTo("invited " + userId + " job " + jobId);
    }

    @Test
    void listUsersRendersEmptyPlaceholder() {
        when(users.list()).thenReturn(List.of());
        assertThat(tools.listUsers()).isEqualTo("(none)");
    }

    @Test
    void listUsersJoinsRows() {
        AppUser admin = new AppUser();
        admin.setEmail("admin@notif.local");
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(UserRole.ADMIN);
        when(users.list()).thenReturn(List.of(admin));

        assertThat(tools.listUsers()).isEqualTo("admin@notif.local ACTIVE ADMIN");
    }

    @Test
    void updateKitDelegatesToUserService() {
        UUID userId = UUID.randomUUID();
        Map<String, Object> kit = Map.of("version", 1);

        assertThat(tools.updateKit(userId.toString(), kit)).contains(userId.toString());
        verify(users).updateKit(userId, kit, null, null);
    }

    @Test
    void sendTestDeliveryQueuesJob() {
        UUID jobId = UUID.randomUUID();
        when(testDelivery.send(DeliveryChannelType.slack, "C1")).thenReturn(jobId);

        assertThat(tools.sendTestDelivery("slack", "C1")).isEqualTo("queued " + jobId);
    }

    @Test
    void listDeliveryJobsRendersEmptyPlaceholder() {
        when(deliveryService.listRecent()).thenReturn(List.of());
        assertThat(tools.listDeliveryJobs()).isEqualTo("(none)");
    }

    @Test
    void listDeliveryJobsJoinsFields() {
        DeliveryJob job = new DeliveryJob();
        job.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        job.setPurpose(DeliveryPurpose.TEST);
        job.setChannel(DeliveryChannelType.email);
        job.setStatus(DeliveryStatus.sent);
        job.setRecipient("ada@notif.local");
        when(deliveryService.listRecent()).thenReturn(List.of(job));

        assertThat(tools.listDeliveryJobs())
                .contains("TEST")
                .contains("email")
                .contains("sent")
                .contains("ada@notif.local");
    }
}
