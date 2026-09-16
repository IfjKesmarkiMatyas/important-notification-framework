package com.notif.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PushoverChannelTest {

    @Test
    void failsWhenAppTokenMissing() {
        PushoverChannel channel = new PushoverChannel(builder(), "");
        DeliveryJob job = new DeliveryJob();
        job.setRecipient("ukey");

        ChannelResult result = channel.send(job);

        assertThat(channel.type()).isEqualTo(DeliveryChannelType.pushover);
        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Pushover app token is not configured");
    }

    @Test
    void failsWhenUserKeyMissingEvenIfAppTokenPresent() {
        PushoverChannel channel = new PushoverChannel(builder(), "apptoken");
        DeliveryJob job = new DeliveryJob();
        job.setRecipient("  ");

        ChannelResult result = channel.send(job);

        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Pushover user key is missing");
    }

    private static RestClient.Builder builder() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(RestClient.class));
        return builder;
    }
}
