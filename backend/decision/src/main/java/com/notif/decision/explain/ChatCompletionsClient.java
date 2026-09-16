package com.notif.decision.explain;

public interface ChatCompletionsClient {
    String complete(String systemPrompt, String userPrompt);
}
