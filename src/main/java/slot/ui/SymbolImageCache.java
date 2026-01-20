package slot.ui;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.image.Image;
import slot.model.Symbol;

public class SymbolImageCache {
    private final Map<Symbol, Image> images = new EnumMap<>(Symbol.class);

    public SymbolImageCache() {
        for (Symbol symbol : Symbol.values()) {
            images.put(symbol, load(symbol.getImagePath()));
        }
    }

    public Image get(Symbol symbol) {
        return images.get(symbol);
    }

    private Image load(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        InputStream stream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(normalized),
                "No se encontro recurso: " + path
        );
        return new Image(stream);
    }
}
