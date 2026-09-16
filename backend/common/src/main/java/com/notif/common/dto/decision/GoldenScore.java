package com.notif.common.dto.decision;

import java.util.List;

public record GoldenScore(
        String engine,
        int n,
        int tp,
        int fp,
        int fn,
        int tn,
        Double precision,
        Double recall,
        Double f1,
        int fireLevelChecked,
        int fireLevelOk,
        List<Slice> byFamily,
        List<Slice> byUser,
        List<FireCase> fires,
        List<Mismatch> mismatches
) {
    public record Slice(String name, int n, int tp, int fp, int fn, int tn, Double f1) {}

    public record FireCase(
            String caseId,
            String user,
            String family,
            String level,
            List<String> channels,
            String reason
    ) {}

    public record Mismatch(String caseId, String user, String golden, String engine, String type) {}
}
