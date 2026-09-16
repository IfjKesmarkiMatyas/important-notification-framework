package com.notif.decision.eval;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.notif.decision.explain.LevelResolver;
import com.notif.decision.explain.NativeExplainer;
import com.notif.decision.match.InterestMatcher;
import com.notif.decision.policy.DecisionPolicy;
import com.notif.decision.service.NativeDecisionEngine;
import tools.jackson.databind.json.JsonMapper;

class GoldenTruthTest {

    @Test
    void nativeEngineScoresPerfectF1OnSixtyFiveSamples() {
        DecisionPolicy policy = new DecisionPolicy();
        NativeDecisionEngine engine = new NativeDecisionEngine(
                new InterestMatcher(policy),
                new LevelResolver(),
                new NativeExplainer()
        );
        GoldenEvaluator evaluator = new GoldenEvaluator(engine, new JsonMapper());

        var score = evaluator.score();

        assertThat(score.mismatches())
                .as("native mismatches: %s", score.mismatches())
                .isEmpty();
        assertThat(score.n()).isEqualTo(65);
        assertThat(score.tp()).isEqualTo(7);
        assertThat(score.fp()).isZero();
        assertThat(score.fn()).isZero();
        assertThat(score.tn()).isEqualTo(58);
        assertThat(score.f1()).isEqualTo(1.0);
    }
}
