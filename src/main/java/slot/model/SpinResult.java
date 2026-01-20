package slot.model;

public class SpinResult {
    private final Symbol[][] window;
    private final SpinStops stops;

    public SpinResult(Symbol[][] window, SpinStops stops) {
        this.window = window;
        this.stops = stops;
    }

    public Symbol[][] getWindow() {
        return window;
    }

    public SpinStops getStops() {
        return stops;
    }

    public Symbol getAt(int row, int reel) {
        return window[row][reel];
    }
}
