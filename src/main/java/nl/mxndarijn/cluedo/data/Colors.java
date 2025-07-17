package nl.mxndarijn.cluedo.data;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public enum Colors {
    LICHTGROEN("light-green", ChatColor.GREEN, "Lime", Material.LIME_SHULKER_BOX, "light-green-block", "\uE035"),
    GROEN("green", ChatColor.DARK_GREEN, "Groen", Material.GREEN_SHULKER_BOX, "green-block", "\uE032"),
    LICHTBLAUW("light-blue", ChatColor.BLUE, "Licht-Blauw", Material.LIGHT_BLUE_SHULKER_BOX, "light-blue-block", "\uE033"),
    BLAUW("blue", ChatColor.DARK_BLUE, "Blauw", Material.BLUE_SHULKER_BOX, "blue-block", "\uE027"),
    ZWART("black", ChatColor.BLACK, "Zwart", Material.BLACK_SHULKER_BOX, "black-block", "\uE042"),
    WIT("white", ChatColor.WHITE, "Wit", Material.WHITE_SHULKER_BOX, "white-block", "\uE041"),
    ORANJE("orange", ChatColor.GOLD, "Oranje", Material.ORANGE_SHULKER_BOX, "orange-block", "\uE037"),
    ROOD("red", ChatColor.RED, "Rood", Material.RED_SHULKER_BOX, "red-block", "\uE039"),
    MAGENTA("magenta", ChatColor.LIGHT_PURPLE, "Magenta", Material.MAGENTA_SHULKER_BOX, "magenta-block", "\uE036"),
    ROZE("pink", ChatColor.LIGHT_PURPLE, "Roze", Material.PINK_SHULKER_BOX, "pink-block", "\uE040"),
    PAARS("purple", ChatColor.DARK_PURPLE, "Paars", Material.PURPLE_SHULKER_BOX, "purple-block", "\uE038"),
    CYAN("cyan", ChatColor.DARK_AQUA, "Cyan", Material.CYAN_SHULKER_BOX, "cyan-block", "\uE0229"),
    BRUIN("brown", ChatColor.GOLD, "Bruin", Material.BROWN_SHULKER_BOX, "brown-block", "\uE028"),
    LICHTGRIJS("light-gray", ChatColor.GRAY, "Licht-Grijs", Material.LIGHT_GRAY_SHULKER_BOX, "light-gray-block", "\uE034"),
    GRIJS("gray", ChatColor.DARK_GRAY, "Grijs", Material.GRAY_SHULKER_BOX, "gray-block", "\uE031"),
    GEEL("yellow", ChatColor.YELLOW, "Geel", Material.YELLOW_SHULKER_BOX, "yellow-block", "\uE030");

    private final String type;
    private final ChatColor color;
    private final String displayName;
    private final String displayNameWithoutColor;
    private final Material shulkerBlock;
    private final String headKey;
    private final String unicodeIcon;

    private final String PREFIX = ChatColor.WHITE + "\uE001\uE001\uE001\uE001\uE001";

    Colors(String type, ChatColor color, String displayName, Material shulkerBlock, String headKey, String unicodeIcon) {
        this.type = type;
        this.color = color;
        this.displayName = this.color + displayName;
        this.displayNameWithoutColor = displayName;
        this.shulkerBlock = shulkerBlock;
        this.headKey = headKey;
        this.unicodeIcon = "  " + PREFIX + unicodeIcon + "  ";
    }

    public static Optional<Colors> getColorByType(String type) {
        for (Colors color : values()) {
            if (color.type.equals(type)) {
                return Optional.of(color);
            }
        }

        return Optional.empty();
    }

    public static Optional<Colors> getColorByMaterial(Material type) {
        for (Colors color : values()) {
            if (color.shulkerBlock.equals(type)) {
                return Optional.of(color);
            }
        }

        return Optional.empty();
    }

    public String getType() {
        return type;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getShulkerBlock() {
        return shulkerBlock;
    }

    public String getHeadKey() {
        return headKey;
    }

    public String getDisplayNameWithoutColor() {
        return displayNameWithoutColor;
    }

    public String getUnicodeIcon() {
        return unicodeIcon;
    }
}
