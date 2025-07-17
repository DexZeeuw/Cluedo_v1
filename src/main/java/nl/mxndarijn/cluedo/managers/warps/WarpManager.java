package nl.mxndarijn.cluedo.managers.warps;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class WarpManager {

    private final File warpFile;
    private final List<Warp> warps;

    public WarpManager(File f) {
        this.warpFile = f;
        if (!this.warpFile.exists()) {
            try {
                this.warpFile.createNewFile();
            } catch (IOException e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not create warp file (" + f.getAbsolutePath() + ")");
                e.printStackTrace();
            }
        }
        FileConfiguration fc = YamlConfiguration.loadConfiguration(warpFile);
        warps = Warp.getWarpsFromFile(f, fc);
    }

    public List<Warp> getWarps() {
        return warps;
    }

    public void save() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(warpFile);
        fc.getKeys(false).forEach(k -> {
            fc.set(k, null);
        });
        warps.forEach(w -> {
            w.save(warpFile, fc);
        });
        try {
            fc.save(warpFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not save file. (" + warpFile.getAbsolutePath() + ")");
            e.printStackTrace();
        }
    }

    public void addWarp(Warp w) {
        warps.add(w);
        save();
    }

    public void removeWarp(Warp w) {
        warps.remove(w);
        save();
    }

    public File getWarpFile() {
        return warpFile;
    }

    public Optional<Warp> getWarpByName(String warpName) {
        for (Warp warp : warps) {
            if (warp.getName().equals(warpName))
                return Optional.of(warp);
        }
        return Optional.empty();
    }
}
