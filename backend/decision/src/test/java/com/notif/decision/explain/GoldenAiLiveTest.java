package com.notif.decision.explain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.notif.decision.config.DecisionProperties;
import tools.jackson.databind.json.JsonMapper;

@EnabledIfEnvironmentVariable(named = "GPT_API_KEY", matches = ".+")
class GoldenAiLiveTest {

    @Test
    void liveModelReturnsReasonJson() {
        DecisionProperties properties = new DecisionProperties();
        properties.setOpenaiApiKey(System.getenv("GPT_API_KEY"));
        AiExplainer explainer = new AiExplainer(new OpenAiChatCompletionsClient(properties, new JsonMapper()), properties, new JsonMapper());
        org.assertj.core.api.Assertions.assertThat(explainer.configured()).isTrue();
    }
}
