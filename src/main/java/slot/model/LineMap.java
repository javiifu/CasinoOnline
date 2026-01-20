package slot.model;

import java.util.List;

public class LineMap {
    private final List<int[]> lines;

    public LineMap(List<int[]> lines) {
        this.lines = List.copyOf(lines);
    }

    public int lineCount() {
        return lines.size();
    }

    public int[] getLine(int index) {
        return lines.get(index);
    }
}
