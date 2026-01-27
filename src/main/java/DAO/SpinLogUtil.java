package DAO;

import slot.model.SpinResult;

public final class SpinLogUtil {
    private SpinLogUtil() {
    }

    public static String windowToJson(SpinResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int row = 0; row < 3; row++) {
            if (row > 0) {
                sb.append(',');
            }
            sb.append('[');
            for (int reel = 0; reel < 5; reel++) {
                if (reel > 0) {
                    sb.append(',');
                }
                sb.append('"').append(result.getAt(row, reel).name()).append('"');
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public static String stopsToJson(int[] stops) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < stops.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(stops[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
