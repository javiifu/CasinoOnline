package slot.model;

import java.util.EnumMap;
import java.util.Map;

public class Paytable {
    private final Map<Symbol, int[]> pays = new EnumMap<>(Symbol.class);
    private final int[] scatterPays;

    public Paytable(int[] scatterPays) {
        this.scatterPays = scatterPays.clone();
    }

    public void setPays(Symbol symbol, int pay3, int pay4, int pay5) {
        pays.put(symbol, new int[]{0, 0, pay3, pay4, pay5});
    }

    public int getPay(Symbol symbol, int count) {
        int[] table = pays.get(symbol);
        if (table == null || count < 3 || count > 5) {
            return 0;
        }
        return table[count];
    }

    public int getScatterPay(int count) {
        if (count < 0 || count >= scatterPays.length) {
            return 0;
        }
        return scatterPays[count];
    }
}
