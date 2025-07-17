package nl.mxndarijn.cluedo.data;

public enum CustomLabel {

    ICON_BLUE("\uE027"),
    ICON_BROWN("\uE028"),
    ICON_CYAN("\uE029"),
    ICON_YELLOW("\uE030"),
    ICON_GRAY("\uE031"),
    ICON_GREEN("\uE032"),
    ICON_LIGHT_BLUE("\uE033"),
    ICON_LIGHT_GRAY("\uE034"),
    ICON_LIGHT_GREEN("\uE035"),
    ICON_MAGENTA("\uE036"),
    ICON_ORANGE("\uE037"),
    ICON_PURPLE("\uE038"),
    ICON_RED("\uE039"),
    ICON_PINK("\uE040"),
    ICON_WHITE("\uE041"),
    ICON_BLACK("\uE042"),

    TEXT_BLUE("\uE011"),
    TEXT_BROWN("\uE012"),
    TEXT_CYAN("\uE013"),
    TEXT_YELLOW("\uE014"),
    TEXT_GRAY("\uE015"),
    TEXT_GREEN("\uE016"),
    TEXT_LIGHT_BLUE("\uE017"),
    TEXT_LIGHT_GRAY("\uE018"),
    TEXT_LIGHT_GREEN("\uE019"),
    TEXT_MAGENTA("\uE020"),
    TEXT_ORANGE("\uE021"),
    TEXT_PURPLE("\uE022"),
    TEXT_RED("\uE023"),
    TEXT_PINK("\uE024"),
    TEXT_WHITE("\uE025"),
    TEXT_BLACK("\uE026");

    private final String unicodeCharacter;

    CustomLabel(String unicodeCharacter) {
        this.unicodeCharacter = unicodeCharacter;
    }

    @Override
    public String toString() {
        return unicodeCharacter;
    }

    public String getUnicodeCharacter() {
        return unicodeCharacter;
    }
}