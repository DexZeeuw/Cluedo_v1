package nl.mxndarijn.cluedo.data;

public enum ChestAppearance {
    CHOICE_TWO("Twee-Items-Keuze", CustomInventoryOverlay.CHEST_APPEARANCE_2_OPTIONS.getUnicodeCharacter()),
    CHOICE_THREE("Drie-Items-Keuze", CustomInventoryOverlay.CHEST_APPEARANCE_3_OPTIONS.getUnicodeCharacter()),
    CHOICE_FOUR("Vier-Items-Keuze", CustomInventoryOverlay.CHEST_APPEARANCE_4_OPTIONS.getUnicodeCharacter());

    private final String name;
    private final String unicode;

    ChestAppearance(String name, String unicodeInventoryName) {
        this.name = name;
        this.unicode = unicodeInventoryName;

    }

    public String getName() {
        return name;
    }

    public String getUnicode() {
        return unicode;
    }
}
