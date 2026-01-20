package slot.db;

import slot.model.SpinResult;

public final class SpinLogUtil {
    private SpinLogUtil() {
    }

    public static String windowToCompactString(SpinResult result) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 3; row++) {
            for (int reel = 0; reel < 5; reel++) {
                if (sb.length() > 0) {
                    sb.append('|');
                }
                sb.append(result.getAt(row, reel).name());
            }
        }
        return sb.toString();
    }
}
