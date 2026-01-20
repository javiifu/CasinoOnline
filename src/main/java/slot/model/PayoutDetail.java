package slot.model;

import java.util.List;

public record PayoutDetail(
        List<LineWin> lineWins,
        int scatterCount,
        int scatterWin,
        boolean bonusTriggered,
        int bonusWin,
        int totalWin
) {
}
