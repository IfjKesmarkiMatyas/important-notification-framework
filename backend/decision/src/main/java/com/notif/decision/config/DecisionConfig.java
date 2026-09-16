package com.notif.decision.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.notif.decision.policy.DecisionPolicy;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(DecisionProperties.class)
public class DecisionConfig {

    @Bean
    DecisionPolicy decisionPolicy(JsonMapper jsonMapper) throws Exception {
        ClassPathResource resource = new ClassPathResource("decision-policy.json");
        try (var in = resource.getInputStream()) {
            return jsonMapper.readValue(in, DecisionPolicy.class);
        }
    }
}
