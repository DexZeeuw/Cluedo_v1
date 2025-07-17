package nl.mxndarijn.cluedo.items.util.storage;

public enum StorageContainerConfigValue {

    ITEMS("items"),
    NAME("name"),
    SKULL("skull"),
    OWNER("owner"),
    IS_PUBLIC("is-public");

    private final String configValue;

    StorageContainerConfigValue(String name) {
        this.configValue = name;
    }

    public String getConfigValue() {
        return configValue;
    }

    @Override
    public String toString() {
        return configValue;
    }
}
