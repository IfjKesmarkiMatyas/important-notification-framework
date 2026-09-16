package com.notif.decision.explain;

import org.springframework.stereotype.Component;
import com.notif.common.entity.scrape.NormalizedEvent;
import com.notif.decision.match.InterestHit;
import com.notif.decision.parse.Maps;

@Component
public class NativeExplainer {

    public String explain(NormalizedEvent event, InterestHit hit) {
        boolean en = event != null && "en".equalsIgnoreCase(event.getLocale());
        if (hit == null) {
            return noMatch(event);
        }
        return switch (hit.type()) {
            case "disaster" -> disaster(en, event, hit);
            case "market" -> market(en, hit);
            case "breaking" -> breaking(en, hit);
            default -> noMatch(event);
        };
    }

    public String noMatch(NormalizedEvent event) {
        boolean en = event != null && "en".equalsIgnoreCase(event.getLocale());
        return en ? "No matching interest." : "Nincs illeszkedő érdeklődés.";
    }

    public String inactive(NormalizedEvent event) {
        boolean en = event != null && "en".equalsIgnoreCase(event.getLocale());
        return en ? "User is not ACTIVE." : "A felhasználó nem ACTIVE.";
    }

    private static String disaster(boolean en, NormalizedEvent event, InterestHit hit) {
        String place = event == null ? "" : Maps.str(event.getPayload() == null ? null : event.getPayload().get("place"));
        String mag = format(hit.eventValue());
        String min = format(hit.threshold());
        if (en) {
            return "Earthquake M" + mag + " ≥ " + min + (place.isBlank() ? "." : " (" + place + ").");
        }
        return "Földrengés M" + mag + " ≥ " + min + (place.isBlank() ? "." : " (" + place + ").");
    }

    private static String market(boolean en, InterestHit hit) {
        String instrument = hit.instrument() == null ? "" : hit.instrument();
        String move = format(hit.eventValue());
        String min = format(hit.threshold());
        if (en) {
            return instrument + " moved " + move + "% ≥ " + min + "%.";
        }
        return instrument + " elmozdulás " + move + "% ≥ " + min + "%.";
    }

    private static String breaking(boolean en, InterestHit hit) {
        String topics = String.join(", ", hit.matchedTopics());
        if (en) {
            return "Breaking topics matched: " + topics + ".";
        }
        return "Breaking témaillesztés: " + topics + ".";
    }

    private static String format(Double value) {
        if (value == null) {
            return "?";
        }
        if (value == Math.rint(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }
}
