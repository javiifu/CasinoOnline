package slot.model;

public enum Symbol {
    BAR("Vista/Imagenes/Slot/Bar.png"),
    TWO_BAR("Vista/Imagenes/Slot/DobleBar.png"),
    COLLAR("Vista/Imagenes/Slot/Collar.png"),
    CORONA("Vista/Imagenes/Slot/Corona.png"),
    LINGOTE("Vista/Imagenes/Slot/Lingote.png"),
    DIAMANTE("Vista/Imagenes/Slot/Diamante.png"),
    MONEDA("Vista/Imagenes/Slot/MonedaDef.png"),
    SCATTER("Vista/Imagenes/Slot/Scatter.png"),
    WILD("Vista/Imagenes/Slot/WILD.png");

    private final String imagePath;

    Symbol(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isWild() {
        return this == WILD;
    }

    public boolean isScatter() {
        return this == SCATTER;
    }
}
