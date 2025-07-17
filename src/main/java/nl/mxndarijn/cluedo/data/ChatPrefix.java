package nl.mxndarijn.cluedo.data;

import org.bukkit.ChatColor;

public enum ChatPrefix {
    WIDM(ChatColor.GREEN + "WIDM"),
    NO_PERMISSION(ChatColor.RED + "Geen-Permissie");

    private final String prefix;
    private final String name;

    ChatPrefix(String prefix) {
        this.prefix = prefix + ChatColor.DARK_GREEN + " \u00BB " + ChatColor.GRAY;
        this.name = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }
}
