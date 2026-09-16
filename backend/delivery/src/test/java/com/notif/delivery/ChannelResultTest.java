package com.notif.delivery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelResultTest {

    @Test
    void successHasNoError() {
        ChannelResult result = ChannelResult.success();
        assertThat(result.ok()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void failureKeepsMessage() {
        ChannelResult result = ChannelResult.failure("nope");
        assertThat(result.ok()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("nope");
    }
}
