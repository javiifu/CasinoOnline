package slot.sim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import slot.engine.GameService;
import slot.engine.PayoutEvaluator;
import slot.engine.SpinEngine;
import slot.engine.SpinOutcome;
import slot.model.SlotConfig;
import slot.model.SlotConfigFactory;
import slot.rng.SplittableRandomSource;

public class SlotSimulator {
    public static void main(String[] args) throws IOException {
        long spins = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Long seed = args.length > 1 ? Long.parseLong(args[1]) : null;

        SlotConfig config = SlotConfigFactory.createDefault(1);
        SplittableRandomSource rng = seed == null ? new SplittableRandomSource() : new SplittableRandomSource(seed);
        GameService service = new GameService(new SpinEngine(rng), new PayoutEvaluator());

        double[] returns = new double[(int) spins];
        long wins = 0;
        double sum = 0.0;
        double sumSq = 0.0;
        int[] scatterDist = new int[16];

        for (int i = 0; i < spins; i++) {
            SpinOutcome outcome = service.spin(config);
            int win = outcome.payoutDetail().totalWin();
            double ret = (double) win / config.betTotal();
            returns[i] = ret;
            sum += win;
            sumSq += (double) win * win;
            if (win > 0) {
                wins++;
            }
            int sc = outcome.payoutDetail().scatterCount();
            if (sc >= 0 && sc < scatterDist.length) {
                scatterDist[sc]++;
            }
        }

        double meanWin = sum / spins;
        double rtp = meanWin / config.betTotal();
        double variance = (sumSq / spins) - (meanWin * meanWin);
        double stdDev = Math.sqrt(Math.max(variance, 0.0));
        double vi = stdDev / config.betTotal();

        Arrays.sort(returns);
        double p95 = returns[(int) Math.floor(spins * 0.95)];
        double p99 = returns[(int) Math.floor(spins * 0.99)];
        double hitRate = (double) wins / spins;

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("slot_simulation.csv"))) {
            writer.write("spins,rtp,hitRate,vi,p95,p99\n");
            writer.write(spins + "," + rtp + "," + hitRate + "," + vi + "," + p95 + "," + p99 + "\n");
            writer.write("\nscatter_count,frequency\n");
            for (int i = 0; i < scatterDist.length; i++) {
                writer.write(i + "," + scatterDist[i] + "\n");
            }
        }
    }
}
