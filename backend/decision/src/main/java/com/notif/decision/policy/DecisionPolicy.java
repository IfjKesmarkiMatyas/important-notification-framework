package com.notif.decision.policy;

public class DecisionPolicy {

    private String magnitudeCompare = "gte";
    private String moveCompare = "gte";
    private String topicMode = "any";

    public String getMagnitudeCompare() {
        return magnitudeCompare;
    }

    public void setMagnitudeCompare(String magnitudeCompare) {
        this.magnitudeCompare = magnitudeCompare;
    }

    public String getMoveCompare() {
        return moveCompare;
    }

    public void setMoveCompare(String moveCompare) {
        this.moveCompare = moveCompare;
    }

    public String getTopicMode() {
        return topicMode;
    }

    public void setTopicMode(String topicMode) {
        this.topicMode = topicMode;
    }

    public boolean passes(double actual, double threshold, String compare) {
        if ("gt".equalsIgnoreCase(compare)) {
            return actual > threshold;
        }
        return actual >= threshold;
    }
}
