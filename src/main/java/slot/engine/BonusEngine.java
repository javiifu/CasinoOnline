package slot.engine;

import slot.model.SlotConfig;

public class BonusEngine {
    public int bonusWin(SlotConfig config) {
        // TODO: sustituir por simulacion real del bonus.
        return config.betTotal() * 10;
    }
}
