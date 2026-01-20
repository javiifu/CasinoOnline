package slot.engine;

import java.util.List;
import slot.model.ReelStrip;
import slot.model.SlotConfig;
import slot.model.SpinResult;
import slot.model.SpinStops;
import slot.model.Symbol;
import slot.rng.RandomSource;

public class SpinEngine {
    private final RandomSource randomSource;

    public SpinEngine(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    public SpinResult spin(SlotConfig cfg) {
        List<ReelStrip> reels = cfg.reels();
        int[] stops = new int[reels.size()];
        Symbol[][] window = new Symbol[3][reels.size()];

        for (int r = 0; r < reels.size(); r++) {
            ReelStrip reel = reels.get(r);
            int stopIndex = randomSource.nextInt(reel.length());
            stops[r] = stopIndex;
            Symbol[] win = reel.getWindow(stopIndex);
            window[0][r] = win[0];
            window[1][r] = win[1];
            window[2][r] = win[2];
        }

        return new SpinResult(window, new SpinStops(stops));
    }
}
