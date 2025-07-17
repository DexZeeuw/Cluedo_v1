package nl.mxndarijn.cluedo.map.mapplayer;

public enum MapPlayerConfigValue {
    LOCATION("location"),
    ROLE("role"),
    IS_PEACEKEEPER("is-peacekeeper");
    private final String configValue;

    MapPlayerConfigValue(String value) {
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
