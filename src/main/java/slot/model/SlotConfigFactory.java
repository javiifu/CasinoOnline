package slot.model;

import java.util.ArrayList;
import java.util.List;

public final class SlotConfigFactory {
    private SlotConfigFactory() {
    }

    public static SlotConfig createDefault(int betLine) {
        // TODO: Sustituir por reels reales (conteos por simbolo por rodillo).
        List<ReelStrip> reels = new ArrayList<>();
        reels.add(new ReelStrip(List.of(
                Symbol.BAR, Symbol.COLLAR, Symbol.DIAMANTE, Symbol.WILD, Symbol.CORONA,
                Symbol.TWO_BAR, Symbol.LINGOTE, Symbol.MONEDA, Symbol.SCATTER, Symbol.CORONA,
                Symbol.BAR, Symbol.LINGOTE, Symbol.COLLAR, Symbol.MONEDA, Symbol.DIAMANTE,
                Symbol.TWO_BAR, Symbol.CORONA, Symbol.LINGOTE, Symbol.MONEDA, Symbol.BAR
        )));
        reels.add(new ReelStrip(List.of(
                Symbol.CORONA, Symbol.BAR, Symbol.LINGOTE, Symbol.MONEDA, Symbol.SCATTER,
                Symbol.DIAMANTE, Symbol.TWO_BAR, Symbol.COLLAR, Symbol.WILD, Symbol.CORONA,
                Symbol.BAR, Symbol.LINGOTE, Symbol.MONEDA, Symbol.DIAMANTE, Symbol.TWO_BAR,
                Symbol.COLLAR, Symbol.BAR, Symbol.MONEDA, Symbol.LINGOTE, Symbol.CORONA
        )));
        reels.add(new ReelStrip(List.of(
                Symbol.LINGOTE, Symbol.MONEDA, Symbol.BAR, Symbol.DIAMANTE, Symbol.SCATTER,
                Symbol.COLLAR, Symbol.TWO_BAR, Symbol.CORONA, Symbol.WILD, Symbol.MONEDA,
                Symbol.BAR, Symbol.CORONA, Symbol.LINGOTE, Symbol.MONEDA, Symbol.DIAMANTE,
                Symbol.TWO_BAR, Symbol.COLLAR, Symbol.BAR, Symbol.MONEDA, Symbol.CORONA
        )));
        reels.add(new ReelStrip(List.of(
                Symbol.DIAMANTE, Symbol.CORONA, Symbol.BAR, Symbol.MONEDA, Symbol.SCATTER,
                Symbol.LINGOTE, Symbol.TWO_BAR, Symbol.COLLAR, Symbol.WILD, Symbol.CORONA,
                Symbol.BAR, Symbol.MONEDA, Symbol.LINGOTE, Symbol.DIAMANTE, Symbol.TWO_BAR,
                Symbol.COLLAR, Symbol.BAR, Symbol.MONEDA, Symbol.LINGOTE, Symbol.CORONA
        )));
        reels.add(new ReelStrip(List.of(
                Symbol.COLLAR, Symbol.BAR, Symbol.MONEDA, Symbol.LINGOTE, Symbol.SCATTER,
                Symbol.DIAMANTE, Symbol.TWO_BAR, Symbol.CORONA, Symbol.WILD, Symbol.MONEDA,
                Symbol.BAR, Symbol.LINGOTE, Symbol.MONEDA, Symbol.DIAMANTE, Symbol.TWO_BAR,
                Symbol.COLLAR, Symbol.BAR, Symbol.MONEDA, Symbol.LINGOTE, Symbol.CORONA
        )));

        // TODO: Ajustar tabla de pagos real (multiplicadores por linea).
        Paytable paytable = new Paytable(new int[]{0, 0, 0, 2, 10, 50});
        paytable.setPays(Symbol.BAR, 5, 20, 100);
        paytable.setPays(Symbol.TWO_BAR, 5, 20, 120);
        paytable.setPays(Symbol.COLLAR, 5, 25, 140);
        paytable.setPays(Symbol.CORONA, 8, 30, 180);
        paytable.setPays(Symbol.LINGOTE, 10, 40, 200);
        paytable.setPays(Symbol.DIAMANTE, 12, 50, 250);
        paytable.setPays(Symbol.MONEDA, 4, 15, 80);
        paytable.setPays(Symbol.WILD, 15, 60, 300);

        LineMap lineMap = new LineMap(create20Lines());

        return new SlotConfig(reels, lineMap, paytable, 20, betLine, 3);
    }

    private static List<int[]> create20Lines() {
        List<int[]> lines = new ArrayList<>();
        lines.add(new int[]{1, 1, 1, 1, 1});
        lines.add(new int[]{0, 0, 0, 0, 0});
        lines.add(new int[]{2, 2, 2, 2, 2});
        lines.add(new int[]{0, 1, 2, 1, 0});
        lines.add(new int[]{2, 1, 0, 1, 2});
        lines.add(new int[]{0, 0, 1, 0, 0});
        lines.add(new int[]{2, 2, 1, 2, 2});
        lines.add(new int[]{1, 2, 2, 2, 1});
        lines.add(new int[]{1, 0, 0, 0, 1});
        lines.add(new int[]{0, 1, 1, 1, 0});
        lines.add(new int[]{2, 1, 1, 1, 2});
        lines.add(new int[]{0, 1, 0, 1, 0});
        lines.add(new int[]{2, 1, 2, 1, 2});
        lines.add(new int[]{1, 0, 1, 2, 1});
        lines.add(new int[]{1, 2, 1, 0, 1});
        lines.add(new int[]{0, 1, 2, 2, 2});
        lines.add(new int[]{2, 1, 0, 0, 0});
        lines.add(new int[]{0, 0, 0, 1, 2});
        lines.add(new int[]{2, 2, 2, 1, 0});
        lines.add(new int[]{0, 2, 0, 2, 0});
        return lines;
    }
}
