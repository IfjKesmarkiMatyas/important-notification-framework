package com.notif.delivery.channel;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.web.client.RestClient;
import com.notif.common.domain.delivery.DeliveryChannelType;
import com.notif.common.dto.delivery.ChannelResult;
import com.notif.common.entity.delivery.DeliveryJob;

class SlackChannelTest {

    @Test
    void failsWhenBotTokenMissing() {
        SlackChannel channel = new SlackChannel(builder(), "  ");
        DeliveryJob job = new DeliveryJob();
        job.setRecipient("C123");

        ChannelResult result = channel.send(job);

        assertThat(channel.type()).isEqualTo(DeliveryChannelType.slack);
        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Slack bot token is not configured");
    }

    @Test
    void treatsNullTokenAsMissing() {
        SlackChannel channel = new SlackChannel(builder(), null);
        DeliveryJob job = new DeliveryJob();
        job.setRecipient("U123");

        assertThat(channel.send(job).errorMessage()).contains("not configured");
    }

    private static RestClient.Builder builder() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(RestClient.class));
        return builder;
    }
}
