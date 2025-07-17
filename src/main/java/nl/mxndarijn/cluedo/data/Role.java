package nl.mxndarijn.cluedo.data;

import nl.mxndarijn.cluedo.managers.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public enum Role {
    SPELER("player", ChatColor.GOLD + "Speler", ChatColor.GOLD + "Speler-Peacekeeper", "gold-block", CustomInventoryOverlay.ROLES_PLAYER.getUnicodeCharacter(), Material.GOLD_BLOCK, "<gold>Spelers", "Hebben gewonnen", PlayerData.UserDataType.SPELERWINS),
    MOORDENAAR("moordenaar", ChatColor.DARK_AQUA + "Moordenaar", ChatColor.DARK_AQUA + "Moordenaar-Peacekeeper", "diamond-block", CustomInventoryOverlay.ROLES_MURDERER.getUnicodeCharacter(), Material.DIAMOND_BLOCK, "<#00FFFF>Moordenaars", "Hebben gewonnen", PlayerData.UserDataType.MURDERWINS);

    private final String rolName;
    private final String peacekeeperName;
    private final String roleType;
    private final String headKey;
    private final String unicode;
    private final Material type;
    private final String title;
    private final String subTitle;
    private final PlayerData.UserDataType winType;

    Role(String rolType, String normalName, String peacekeeperName, String headKey, String unicode, Material type, String title, String subTitle, PlayerData.UserDataType winType) {
        this.roleType = rolType;
        this.rolName = normalName;
        this.peacekeeperName = peacekeeperName;
        this.headKey = headKey;
        this.unicode = unicode;
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
        this.winType = winType;
    }

    public static Optional<Role> getRoleByType(String type) {
        for (Role value : values()) {
            if (value.roleType.equalsIgnoreCase(type))
                return Optional.of(value);
        }
        return Optional.empty();
    }

    public String getRolName() {
        return rolName;
    }

    public String getPeacekeeperName() {
        return peacekeeperName;
    }

    public String getRoleType() {
        return roleType;
    }

    public String getHeadKey() {
        return headKey;
    }

    public String getUnicode() {
        return unicode;
    }

    public Material getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public PlayerData.UserDataType getWinType() {
        return winType;
    }
}
