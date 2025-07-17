package nl.mxndarijn.cluedo.data;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ScoreBoard {
    MAP(ConfigFiles.SCOREBOARD_MAP),
    PRESET(ConfigFiles.SCOREBOARD_PRESET),
    GAME_HOST(ConfigFiles.SCOREBOARD_HOST),
    GAME_PLAYER(ConfigFiles.SCOREBOARD_PLAYER),
    GAME_SPECTATOR(ConfigFiles.SCOREBOARD_SPECTATOR),
    SPAWN(ConfigFiles.SCOREBOARD_SPAWN);

    private final List<String> uneditedLines;
    private final String title;

    ScoreBoard(ConfigFiles configFile) {
        this.uneditedLines = new ArrayList<>();
        configFile.getFileConfiguration().getStringList("lines").forEach(string -> {
            this.uneditedLines.add(ChatColor.translateAlternateColorCodes('&', string));
        });
        this.title = ChatColor.translateAlternateColorCodes('&', configFile.getFileConfiguration().getString("title", "Unknown"));
    }

    public String getTitle(HashMap<String, String> placeholders) {
        String newTitle = title;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            newTitle = newTitle.replaceAll(k, v);
        }
        return newTitle;
    }

    public List<String> getLines(HashMap<String, String> placeholders) {
        List<String> newLines = new ArrayList<>();

        for (String string : uneditedLines) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                string = string.replaceAll(k, v);
            }
            newLines.add(string);
        }
        return newLines;
    }
}
