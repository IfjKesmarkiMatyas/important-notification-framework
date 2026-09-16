package com.notif.identity.service;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;
import com.notif.common.exception.IdentityException;
import com.notif.common.port.DeliveryDispatcher;

@ExtendWith(MockitoExtension.class)
class TestDeliveryServiceTest {

    @Mock
    private DeliveryDispatcher delivery;

    private TestDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new TestDeliveryService(delivery, "C-TEST", "U-TEST");
    }

    @Test
    void sendUsesExplicitRecipient() {
        UUID jobId = UUID.randomUUID();
        when(delivery.enqueue(any())).thenReturn(jobId);

        UUID id = service.send(DeliveryChannelType.email, "ada@notif.local");

        assertThat(id).isEqualTo(jobId);
        ArgumentCaptor<EnqueueDeliveryCommand> captor = ArgumentCaptor.forClass(EnqueueDeliveryCommand.class);
        verify(delivery).enqueue(captor.capture());
        assertThat(captor.getValue().purpose()).isEqualTo(DeliveryPurpose.TEST);
        assertThat(captor.getValue().channel()).isEqualTo(DeliveryChannelType.email);
        assertThat(captor.getValue().recipient()).isEqualTo("ada@notif.local");
    }

    @Test
    void sendFallsBackToConfiguredSlackAndPushoverTargets() {
        when(delivery.enqueue(any())).thenReturn(UUID.randomUUID());

        service.send(DeliveryChannelType.slack, " ");
        service.send(DeliveryChannelType.pushover, null);

        ArgumentCaptor<EnqueueDeliveryCommand> captor = ArgumentCaptor.forClass(EnqueueDeliveryCommand.class);
        verify(delivery, org.mockito.Mockito.times(2)).enqueue(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(EnqueueDeliveryCommand::recipient)
                .containsExactly("C-TEST", "U-TEST");
    }

    @Test
    void sendRequiresEmailRecipient() {
        assertThatThrownBy(() -> service.send(DeliveryChannelType.email, null))
                .isInstanceOf(IdentityException.class)
                .hasMessage("Recipient required for email")
                .extracting(ex -> ((IdentityException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
