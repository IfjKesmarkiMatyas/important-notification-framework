package com.notif.api.mcp;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.domain.delivery.DeliveryStatus;
import com.notif.common.domain.identity.UserRole;
import com.notif.common.domain.identity.UserStatus;
import com.notif.common.dto.identity.InviteResult;
import com.notif.common.dto.scrape.SourceHealthView;
import com.notif.common.entity.delivery.DeliveryJob;
import com.notif.common.entity.identity.AppUser;
import com.notif.delivery.service.DeliveryService;
import com.notif.decision.eval.GoldenEvaluator;
import com.notif.decision.service.DecisionService;
import com.notif.identity.service.InviteService;
import com.notif.identity.service.TestDeliveryService;
import com.notif.identity.service.UserService;
import com.notif.scrape.service.ScrapeService;
import tools.jackson.databind.json.JsonMapper;

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

    @Mock
    private ScrapeService scrapeService;

    @Mock
    private DecisionService decisions;

    @Mock
    private GoldenEvaluator golden;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private NotifMcpTools tools;

    @Test
    void inviteUserDefaultsLocaleAndReportsIds() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(invites.invite("ada@notif.local", "hu", "MCP"))
                .thenReturn(new InviteResult(userId, jobId));

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

    @Test
    void listScrapeSourcesRendersHealth() {
        when(scrapeService.sources()).thenReturn(List.of(
                new SourceHealthView(
                        "telex", "breaking", "hu", "https://telex.hu/rss", "ok", "ok", Instant.parse("2026-01-01T00:00:00Z"), null, 3)
        ));
        assertThat(tools.listScrapeSources()).contains("telex").contains("breaking").contains("ok");
    }

    @Test
    void runScrapeAllDelegates() {
        when(scrapeService.runAll()).thenReturn(List.of());
        assertThat(tools.runScrape("all")).isEqualTo("(none)");
        verify(scrapeService).runAll();
    }

    @Test
    void exportNormalizedEventsSerializesServicePayload() {
        List<Map<String, Object>> payload = List.of(Map.of("family", "disaster", "sourceId", "usgs"));
        when(scrapeService.exportEvents("disaster", null, 10)).thenReturn(payload);
        when(jsonMapper.writeValueAsString(payload)).thenReturn("[{\"family\":\"disaster\"}]");

        assertThat(tools.exportNormalizedEvents("disaster", null, 10)).contains("disaster");
        verify(scrapeService).exportEvents("disaster", null, 10);
    }

    @Test
    void getDecisionSwitchRendersMode() {
        when(decisions.switchView()).thenReturn(new com.notif.common.dto.decision.SwitchView("native", false));
        assertThat(tools.getDecisionSwitch()).isEqualTo("native openai=false");
    }

    @Test
    void scoreGoldenSerializesEvaluator() {
        var score = new com.notif.common.dto.decision.GoldenScore(65, 7, 0, 0, 58, 1.0, 1.0, 1.0, List.of());
        when(golden.score()).thenReturn(score);
        when(jsonMapper.writeValueAsString(score)).thenReturn("{\"f1\":1.0}");
        assertThat(tools.scoreGolden()).contains("1.0");
    }
}
