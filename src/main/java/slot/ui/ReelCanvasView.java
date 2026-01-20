package slot.ui;

import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import slot.model.ReelStrip;
import slot.model.SlotConfig;
import slot.model.SpinResult;

public class ReelCanvasView extends Canvas {
    private static final int REELS = 5;
    private static final int ROWS = 3;

    private final SlotConfig config;
    private final List<ReelStrip> reels;
    private final SymbolImageCache imageCache;
    private final ReelAnim[] anims = new ReelAnim[REELS];

    private final AnimationTimer timer;
    private Runnable onSpinFinished;
    private SpinResult currentResult;
    private long spinStartNanos;
    private long lastFrameNanos;

    public ReelCanvasView(SlotConfig config, double width, double height) {
        this.config = config;
        this.reels = config.reels();
        this.imageCache = new SymbolImageCache();
        setWidth(width);
        setHeight(height);
        for (int i = 0; i < REELS; i++) {
            anims[i] = new ReelAnim(reels.get(i).length());
        }
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };
        timer.start();
    }

    public void setOnSpinFinished(Runnable onSpinFinished) {
        this.onSpinFinished = onSpinFinished;
    }

    public void startSpin(SpinResult result) {
        this.currentResult = result;
        this.spinStartNanos = System.nanoTime();
        this.lastFrameNanos = spinStartNanos;
        for (int i = 0; i < REELS; i++) {
            anims[i].start(spinStartNanos, result.getStops().stops()[i]);
        }
    }

    private void tick(long now) {
        if (currentResult == null) {
            render();
            lastFrameNanos = now;
            return;
        }

        double deltaSec = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;

        boolean allStopped = true;
        for (int i = 0; i < REELS; i++) {
            ReelAnim anim = anims[i];
            anim.update(now, deltaSec, spinStartNanos, i);
            if (!anim.stopped) {
                allStopped = false;
            }
        }

        render();

        if (allStopped) {
            currentResult = null;
            if (onSpinFinished != null) {
                onSpinFinished.run();
            }
        }
    }

    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double cellW = getWidth() / REELS;
        double cellH = getHeight() / ROWS;

        for (int reelIndex = 0; reelIndex < REELS; reelIndex++) {
            ReelAnim anim = anims[reelIndex];
            ReelStrip strip = reels.get(reelIndex);
            double pos = anim.position % strip.length();
            if (pos < 0) {
                pos += strip.length();
            }

            int baseIndex = (int) Math.floor(pos);
            double frac = pos - baseIndex;
            double x = reelIndex * cellW;

            for (int offset = -1; offset <= 3; offset++) {
                int symbolIndex = baseIndex + offset;
                double y = (offset - 1 - frac) * cellH;
                gc.drawImage(
                        imageCache.get(strip.getAt(symbolIndex)),
                        x, y, cellW, cellH
                );
            }
        }
    }

    private static class ReelAnim {
        private static final double MAX_SPEED = 20.0;
        private static final double EASE_IN_SEC = 0.25;
        private static final double STOP_SEC = 0.55;
        private static final double STOP_DELAY_SEC = 0.14;
        private static final int EXTRA_SPINS = 2;

        private final int length;
        private double position;
        private double speed;
        private boolean stopping;
        private boolean stopped;

        private int targetStop;
        private double stopStartPos;
        private double stopDistance;
        private long stopStartNanos;

        private ReelAnim(int length) {
            this.length = length;
            this.position = 0.0;
            this.speed = 0.0;
        }

        private void start(long spinStartNanos, int targetStop) {
            this.stopping = false;
            this.stopped = false;
            this.targetStop = targetStop;
            this.stopStartNanos = 0;
            this.speed = 0.0;
        }

        private void update(long now, double deltaSec, long spinStartNanos, int reelIndex) {
            if (stopped) {
                return;
            }

            double elapsed = (now - spinStartNanos) / 1_000_000_000.0;
            double stopDelay = reelIndex * STOP_DELAY_SEC + 0.9;
            if (!stopping && elapsed >= stopDelay) {
                beginStop(now);
            }

            if (stopping) {
                double t = (now - stopStartNanos) / 1_000_000_000.0 / STOP_SEC;
                if (t >= 1.0) {
                    position = stopStartPos + stopDistance;
                    position = Math.round(position);
                    stopped = true;
                    stopping = false;
                } else {
                    double eased = easeOut(t);
                    position = stopStartPos + stopDistance * eased;
                }
                return;
            }

            if (elapsed < EASE_IN_SEC) {
                speed = MAX_SPEED * (elapsed / EASE_IN_SEC);
            } else {
                speed = MAX_SPEED;
            }
            position += speed * deltaSec;
        }

        private void beginStop(long now) {
            stopping = true;
            stopStartNanos = now;
            stopStartPos = position;
            double currentIndex = position % length;
            if (currentIndex < 0) {
                currentIndex += length;
            }
            double distanceToTarget = (targetStop - currentIndex + length) % length;
            stopDistance = EXTRA_SPINS * length + distanceToTarget;
        }

        private double easeOut(double t) {
            double inv = 1.0 - t;
            return 1.0 - inv * inv * inv;
        }
    }
}
