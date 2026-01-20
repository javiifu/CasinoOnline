package slot.model;

import java.util.List;

public class ReelStrip {
    private final List<Symbol> symbols;

    public ReelStrip(List<Symbol> symbols) {
        this.symbols = List.copyOf(symbols);
        if (this.symbols.isEmpty()) {
            throw new IllegalArgumentException("Reel strip no puede estar vacio.");
        }
    }

    public int length() {
        return symbols.size();
    }

    public Symbol getAt(int index) {
        int size = symbols.size();
        int wrapped = ((index % size) + size) % size;
        return symbols.get(wrapped);
    }

    public Symbol[] getWindow(int stopIndex) {
        Symbol top = getAt(stopIndex - 1);
        Symbol mid = getAt(stopIndex);
        Symbol bot = getAt(stopIndex + 1);
        return new Symbol[]{top, mid, bot};
    }
}
