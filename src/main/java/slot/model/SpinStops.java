package slot.model;

public record SpinStops(int[] stops) {
    public SpinStops {
        if (stops == null || stops.length != 5) {
            throw new IllegalArgumentException("SpinStops debe tener 5 rodillos.");
        }
    }
}
