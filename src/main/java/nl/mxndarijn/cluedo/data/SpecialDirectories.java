package nl.mxndarijn.cluedo.data;

import nl.mxndarijn.cluedo.Cluedo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SpecialDirectories {
    PRESET_WORLDS("presets"),
    MAP_WORLDS("maps"),
    GAMES_WORLDS("games"),
    LANGUAGE_FILES("languages"),
    STORAGE_FILES("storages");

    private static final List<SpecialDirectories> values = Collections.unmodifiableList(Arrays.asList(values()));
    private final File directory;
    private final String folderName;

    SpecialDirectories(String folderName) {
        this.folderName = folderName;
        JavaPlugin plugin = JavaPlugin.getPlugin(Cluedo.class);
        directory = new File(plugin.getDataFolder() + getPath());
        if (!directory.exists()) {
            boolean success = directory.mkdirs();

        }

    }

    private String getPath() {
        return folderName.startsWith("/") ? folderName : "/" + folderName;
    }

    public File getDirectory() {
        return directory;
    }

    public String getFolderName() {
        return folderName;
    }
}
