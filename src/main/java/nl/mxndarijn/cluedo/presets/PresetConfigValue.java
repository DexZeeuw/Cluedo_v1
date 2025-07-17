package nl.mxndarijn.cluedo.presets;

public enum PresetConfigValue {
    NAME("name"),
    HOST_DIFFICULTY("host-difficulty"),
    PLAY_DIFFICULTY("play-difficulty"),
    COLORS("colors"),
    SKULL_ID("skull-id"),
    LOCKED("locked"),
    LOCKED_BY("locked-by"),
    LOCK_REASON("lock-reason"),
    CONFIGURED("configured");

    private final String configValue;

    PresetConfigValue(String value) {
        this.configValue = value;
    }

    public String getConfigValue() {
        return configValue;
    }

    @Override
    public String toString() {
        return configValue;
    }
}
