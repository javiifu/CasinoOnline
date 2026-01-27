package DAO;

import java.util.UUID;

public record SpinLog(
        UUID roundId,
        UUID betId,
        String gridJson,
        String linesJson,
        String rngSeed,
        String rngNonce,
        boolean bonusTriggered,
        int multiplier
) {
}
