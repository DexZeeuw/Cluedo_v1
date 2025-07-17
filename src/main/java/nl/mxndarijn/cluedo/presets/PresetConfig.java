package nl.mxndarijn.cluedo.presets;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.cluedo.data.Colors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class PresetConfig {
    private final HashMap<Colors, MxLocation> colors;
    private File file;
    private String name;
    private int hostDifficulty;
    private int playDifficulty;
    private String skullId;
    private boolean locked;
    private String lockedBy;
    private String lockReason;
    private boolean configured;

    public PresetConfig(File file) {
        this.file = file;
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        Arrays.stream(PresetConfigValue.values()).forEach(value -> {
            if (!fc.contains(value.getConfigValue())) {
                Logger.logMessage(LogLevel.ERROR, Prefix.PRESETS_MANAGER, "Could not find config value: " + value + " (" + file.getAbsolutePath() + ")");
            }
        });

        name = fc.getString(PresetConfigValue.NAME.getConfigValue());
        hostDifficulty = fc.getInt(PresetConfigValue.HOST_DIFFICULTY.getConfigValue());
        playDifficulty = fc.getInt(PresetConfigValue.PLAY_DIFFICULTY.getConfigValue());
        skullId = fc.getString(PresetConfigValue.SKULL_ID.getConfigValue());

        locked = fc.getBoolean(PresetConfigValue.LOCKED.getConfigValue());
        lockedBy = fc.getString(PresetConfigValue.LOCKED_BY.getConfigValue());
        lockReason = fc.getString(PresetConfigValue.LOCK_REASON.getConfigValue());

        configured = fc.getBoolean(PresetConfigValue.CONFIGURED.getConfigValue());

        colors = new HashMap<>();
        ConfigurationSection colorSection = fc.getConfigurationSection(PresetConfigValue.COLORS.getConfigValue());
        if (colorSection == null) {
            return;
        }
        colorSection.getKeys(false).forEach(key -> {
            Optional<Colors> color = Colors.getColorByType(key);
            if (color.isPresent()) {
                Optional<MxLocation> optionalMxLocation = MxLocation.loadFromConfigurationSection(colorSection.getConfigurationSection(key));
                if (optionalMxLocation.isPresent()) {
                    MxLocation mxLocation = optionalMxLocation.get();
                    colors.put(color.get(), mxLocation);
                } else {
                    Logger.logMessage(LogLevel.ERROR, Prefix.PRESETS_MANAGER, "Could not load spawnpoint for color: " + key + " (" + file.getAbsolutePath() + ")");
                }

            } else {
                Logger.logMessage(LogLevel.ERROR, Prefix.PRESETS_MANAGER, "Could not load color: " + key + " (" + file.getAbsolutePath() + ")");
            }

        });
    }

    public void save() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        fc.set(PresetConfigValue.NAME.getConfigValue(), name);
        fc.set(PresetConfigValue.HOST_DIFFICULTY.getConfigValue(), hostDifficulty);
        fc.set(PresetConfigValue.PLAY_DIFFICULTY.getConfigValue(), playDifficulty);
        fc.set(PresetConfigValue.SKULL_ID.getConfigValue(), skullId);
        fc.set(PresetConfigValue.LOCKED.getConfigValue(), locked);
        fc.set(PresetConfigValue.LOCKED_BY.getConfigValue(), lockedBy);
        fc.set(PresetConfigValue.LOCK_REASON.getConfigValue(), lockReason);
        fc.set(PresetConfigValue.CONFIGURED.getConfigValue(), configured);

        fc.set(PresetConfigValue.COLORS.getConfigValue(), null);

        ConfigurationSection section = fc.createSection(PresetConfigValue.COLORS.getConfigValue());
        for (Colors c : colors.keySet()) {
            colors.get(c).write(section.createSection(c.getType()));
        }

        try {
            fc.save(file);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.PRESETS_MANAGER, "Could not save preset config: " + file.getAbsolutePath());
            e.printStackTrace();
        }

    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHostDifficulty() {
        return hostDifficulty;
    }

    public void setHostDifficulty(int hostDifficulty) {
        this.hostDifficulty = hostDifficulty;
    }

    public int getPlayDifficulty() {
        return playDifficulty;
    }

    public void setPlayDifficulty(int playDifficulty) {
        this.playDifficulty = playDifficulty;
    }

    public String getSkullId() {
        return skullId;
    }

    public void setSkullId(String skullId) {
        this.skullId = skullId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public HashMap<Colors, MxLocation> getColors() {
        return colors;
    }
}
