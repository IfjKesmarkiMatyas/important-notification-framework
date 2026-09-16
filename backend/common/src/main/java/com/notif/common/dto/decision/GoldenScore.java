package com.notif.common.dto.decision;

import java.util.List;

public record GoldenScore(
        int n,
        int tp,
        int fp,
        int fn,
        int tn,
        Double precision,
        Double recall,
        Double f1,
        List<Mismatch> mismatches
) {
    public record Mismatch(String caseId, String user, String golden, String engine, String type) {}
}
