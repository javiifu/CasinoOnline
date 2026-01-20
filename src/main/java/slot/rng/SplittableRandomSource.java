package slot.rng;

import java.util.SplittableRandom;

public class SplittableRandomSource implements RandomSource {
    private final SplittableRandom random;

    public SplittableRandomSource() {
        this.random = new SplittableRandom();
    }

    public SplittableRandomSource(long seed) {
        this.random = new SplittableRandom(seed);
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
