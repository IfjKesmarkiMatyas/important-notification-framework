package com.notif.common.dto.delivery;

public record ChannelResult(boolean ok, String errorMessage) {
    public static ChannelResult success() {
        return new ChannelResult(true, null);
    }

    public static ChannelResult failure(String message) {
        return new ChannelResult(false, message);
    }
}
