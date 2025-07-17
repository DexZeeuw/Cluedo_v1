package nl.mxndarijn.cluedo.data;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.Cluedo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public enum ConfigFiles {
    MAIN_CONFIG("config.yml", "config.yml", false),
    HEAD_DATA("head-data.yml", "head-data.yml", true),
    DEFAULT_LANGUAGE("nl-NL.yml", "languages/nl-NL.yml", false),
    SCOREBOARD_MAP("scoreboard_map.yml", "scoreboards/scoreboard_map.yml", false),
    SCOREBOARD_PRESET("scoreboard_preset.yml", "scoreboards/scoreboard_preset.yml", false),
    SCOREBOARD_HOST("scoreboard_host.yml", "scoreboards/scoreboard_host.yml", false),
    SCOREBOARD_PLAYER("scoreboard_player.yml", "scoreboards/scoreboard_player.yml", false),
    SCOREBOARD_SPECTATOR("scoreboard_spectator.yml", "scoreboards/scoreboard_spectator.yml", false),
    UPCOMING_GAMES("upcoming-games.yml", "upcoming-games.yml", true),
    SCOREBOARD_SPAWN("scoreboard_spawn.yml", "scoreboards/scoreboard_spawn.yml", false);

    private final FileConfiguration fileConfiguration;
    private final File file;
    private final String fileName;

    private final String path;

    private final boolean autoSave;

    ConfigFiles(String fileName, String path, boolean autoSave) {
        this.fileName = fileName;
        this.path = path;
        JavaPlugin plugin = JavaPlugin.getPlugin(Cluedo.class);
        file = new File(plugin.getDataFolder() + File.separator + path);
        if (!file.exists()) {
            Logger.logMessage(LogLevel.INFORMATION, Prefix.CONFIG_FILES, "Could not load: " + file.getName() + ". Trying to load it from internal sources...");
            Functions.copyFileFromResources(fileName, path);
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        this.autoSave = autoSave;
    }

    public static void saveAll() {
        Logger.logMessage(LogLevel.INFORMATION, Prefix.CONFIG_FILES, "Saving all files... ");
        for (ConfigFiles value : values()) {
            if (value.autoSave)
                value.save();
        }
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public void save() {
        try {
            Logger.logMessage(LogLevel.DEBUG, Prefix.CONFIG_FILES, "Saving file... " + path);
            fileConfiguration.save(file);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not save file... " + path);
        }
    }
}
