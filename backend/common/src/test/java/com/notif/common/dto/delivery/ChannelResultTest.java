package com.notif.common.dto.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

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
