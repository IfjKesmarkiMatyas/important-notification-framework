package com.notif.api.station;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.notif.common.domain.decision.DecisionOutcome;
import com.notif.common.dto.decision.DecisionResult;
import com.notif.common.dto.station.StationLoadView;
import com.notif.common.entity.identity.AppUser;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.service.DecisionService;
import com.notif.identity.repository.AppUserRepository;
import com.notif.scrape.repository.NormalizedEventRepository;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private NormalizedEventRepository events;

    @Mock
    private PasswordEncoder passwords;

    @Mock
    private DecisionService decisions;

    @Test
    void loadUpsertsUsersInsertsEventsAndDecidesCohort() {
        StationPack pack = StationPack.load(new JsonMapper());
        StationService real = new StationService(new JsonMapper(), users, events, passwords, decisions);

        when(passwords.encode("stationstation")).thenReturn("hash");
        when(users.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(events.findBySourceIdAndExternalId(any(), any())).thenReturn(Optional.empty());
        when(events.saveAndFlush(any(NormalizedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisions.evaluate(any(NormalizedEvent.class), anyList())).thenReturn(List.of(
                new DecisionResult(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "ada@notif.local",
                        UUID.randomUUID(),
                        "usgs",
                        "us6000tm81",
                        DecisionOutcome.fire,
                        null,
                        "hit",
                        "native",
                        List.of("email"),
                        List.of(),
                        null
                )
        ));

        StationLoadView view = real.load(true);

        assertThat(view.usersUpserted()).isEqualTo(5);
        assertThat(view.eventsInserted()).isEqualTo(pack.events().size());
        assertThat(view.eventsSkipped()).isZero();
        assertThat(view.decisionsRun()).isEqualTo(pack.events().size());
        assertThat(view.fired()).isEqualTo(pack.events().size());
        verify(decisions, times(pack.events().size())).evaluate(any(NormalizedEvent.class), anyList());

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(users, times(5)).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getAllValues())
                .extracting(AppUser::getEmail)
                .contains("ada@notif.local", "denes@notif.local");
        verify(passwords).encode(eq("stationstation"));
    }

    @Test
    void loadCanSkipDecide() {
        StationService real = new StationService(new JsonMapper(), users, events, passwords, decisions);
        when(passwords.encode("stationstation")).thenReturn("hash");
        when(users.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(events.findBySourceIdAndExternalId(any(), any())).thenReturn(Optional.empty());
        when(events.saveAndFlush(any(NormalizedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StationLoadView view = real.load(false);

        assertThat(view.decisionsRun()).isZero();
        assertThat(view.fired()).isZero();
        verify(decisions, times(0)).evaluate(any(NormalizedEvent.class), anyList());
    }
}
