package slot.model;

import java.util.List;

public record SlotConfig(
        List<ReelStrip> reels,
        LineMap lineMap,
        Paytable paytable,
        int lines,
        int betLine,
        int bonusTrigger
) {
    public int betTotal() {
        return lines * betLine;
    }
}
