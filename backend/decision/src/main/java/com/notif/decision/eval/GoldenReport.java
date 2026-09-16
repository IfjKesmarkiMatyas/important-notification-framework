package com.notif.decision.eval;

import java.util.Locale;
import com.notif.common.dto.decision.GoldenScore;

public final class GoldenReport {

    private GoldenReport() {}

    public static String markdown(GoldenScore score) {
        StringBuilder sb = new StringBuilder();
        sb.append("Native golden — ").append(score.engine()).append('\n');
        sb.append(String.format(
                Locale.ROOT,
                "n=%d  TP=%d  FP=%d  FN=%d  TN=%d  P=%s  R=%s  F1=%s  levels %d/%d%n",
                score.n(),
                score.tp(),
                score.fp(),
                score.fn(),
                score.tn(),
                fmt(score.precision()),
                fmt(score.recall()),
                fmt(score.f1()),
                score.fireLevelOk(),
                score.fireLevelChecked()
        ));
        sb.append('\n');
        sb.append("Family\n");
        for (GoldenScore.Slice slice : score.byFamily()) {
            sb.append("  ").append(line(slice)).append('\n');
        }
        sb.append("User\n");
        for (GoldenScore.Slice slice : score.byUser()) {
            sb.append("  ").append(line(slice)).append('\n');
        }
        sb.append('\n');
        sb.append("FIRE cases\n");
        for (GoldenScore.FireCase row : score.fires()) {
            sb.append(String.format(
                    Locale.ROOT,
                    "  %s | %s | %s | %s | %s | %s%n",
                    row.caseId(),
                    row.user(),
                    row.family(),
                    row.level(),
                    String.join(",", row.channels()),
                    row.reason()
            ));
        }
        if (score.mismatches().isEmpty()) {
            sb.append("\nMismatches: none\n");
        } else {
            sb.append("\nMismatches\n");
            for (GoldenScore.Mismatch mismatch : score.mismatches()) {
                sb.append(String.format(
                        "  %s | %s | golden=%s engine=%s (%s)%n",
                        mismatch.caseId(),
                        mismatch.user(),
                        mismatch.golden(),
                        mismatch.engine(),
                        mismatch.type()
                ));
            }
        }
        return sb.toString();
    }

    private static String line(GoldenScore.Slice slice) {
        return String.format(
                Locale.ROOT,
                "%s n=%d TP=%d FP=%d FN=%d TN=%d F1=%s",
                slice.name(),
                slice.n(),
                slice.tp(),
                slice.fp(),
                slice.fn(),
                slice.tn(),
                fmt(slice.f1())
        );
    }

    private static String fmt(Double value) {
        if (value == null) {
            return "—";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
