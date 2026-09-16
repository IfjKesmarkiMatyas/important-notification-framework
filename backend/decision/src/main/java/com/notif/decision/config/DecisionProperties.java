package com.notif.decision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notif.decision")
public class DecisionProperties {

    private String openaiApiKey = "";
    private String model = "gpt-4o-mini";

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey == null ? "" : openaiApiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model == null || model.isBlank() ? "gpt-4o-mini" : model;
    }

    public boolean openaiConfigured() {
        return openaiApiKey != null && !openaiApiKey.isBlank();
    }
}
