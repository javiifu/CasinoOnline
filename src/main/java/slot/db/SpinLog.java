package slot.db;

import java.time.Instant;

public record SpinLog(
        Instant timestamp,
        int betTotal,
        int winTotal,
        String stops,
        String window,
        int scatterCount,
        boolean bonusTriggered
) {
}
