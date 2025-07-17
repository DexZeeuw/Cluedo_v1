package nl.mxndarijn.api.mxworld;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.api.util.VoidGenerator;
import nl.mxndarijn.cluedo.Cluedo;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MxAtlas {
    private static MxAtlas instance;
    private final ArrayList<MxWorld> worlds;

    private MxAtlas() {
        Logger.logMessage(LogLevel.INFORMATION, Prefix.MXATLAS, "Started MxAtlas... (World-Manager)");
        worlds = new ArrayList<>();
    }

    public static MxAtlas getInstance() {
        if (instance == null) {
            instance = new MxAtlas();
        }
        return instance;
    }

    public Optional<MxWorld> getMxWorld(String name) {
        for (MxWorld w : worlds) {
            if (w.getName().equals(name)) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    public Optional<MxWorld> getMxWorld(UUID uuid) {
        for (MxWorld w : worlds) {
            if (w.getUUID().equalsIgnoreCase(uuid.toString())) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    public boolean addMxWorld(MxWorld world) {
        return worlds.add(world);
    }

    public boolean removeMxWorld(MxWorld world) {
        return worlds.remove(world);
    }

    public CompletableFuture<Boolean> loadMxWorld(MxWorld mxWorld) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Loading MxWorld: " + mxWorld.getName());
        if (mxWorld.isLoaded()) {
            Logger.logMessage(LogLevel.WARNING, Prefix.MXATLAS, mxWorld.getName() + " is already loaded.");
            future.complete(true);
            return future;
        }
        String path = mxWorld.getDir().toString().replace("\\", "/");
        WorldCreator wc = new WorldCreator(path);
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new VoidGenerator());
        wc.generateStructures(false);

        BukkitTask task = Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Cluedo.class), () -> {
            World world = wc.createWorld();
            if (world == null) {
                future.complete(false);
                return;
            }
            File worldSettings = new File(mxWorld.getDir() + File.separator + "worldsettings.yml");
            if (!worldSettings.exists()) {
                Functions.copyFileFromResources("worldsettings.yml", worldSettings);
            }
            Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Loading worldsettings.yml... ");
            FileConfiguration worldSettingsCfg = YamlConfiguration.loadConfiguration(worldSettings);
            world.setAutoSave(worldSettingsCfg.getBoolean("autosave"));
            world.setKeepSpawnInMemory(worldSettingsCfg.getBoolean("keepSpawnInMemory"));

            worldSettingsCfg.getConfigurationSection("gamerules").getKeys(false).forEach(val -> {
                world.setGameRuleValue(val, worldSettingsCfg.getConfigurationSection("gamerules").get(val).toString());
            });
            ConfigurationSection spawn = worldSettingsCfg.getConfigurationSection("spawn");
            if (spawn != null) {
                Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Setting spawnlocation... ");
                world.setSpawnLocation(Functions.getLocationFromConfiguration(world, spawn));
            }

            mxWorld.setWorldUID(world.getUID());
            mxWorld.setLoaded(true);

            future.complete(true);
        });

        return future;
    }


    public boolean unloadMxWorld(MxWorld mxWorld, boolean save) {
        if (!mxWorld.isLoaded())
            return true;
        Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Unloading MxWorld: " + mxWorld.getName());
        World w = Bukkit.getWorld(mxWorld.getWorldUID());
        if (w == null) {
            Logger.logMessage(LogLevel.WARNING, Prefix.MXATLAS, "Could not unload MxWorld (World is null): " + mxWorld.getName());
            return false;
        }
        for (Player p : w.getPlayers()) {
            p.teleport(Functions.getSpawnLocation());
        }
        boolean unloaded = Bukkit.unloadWorld(w, save);
        if (unloaded) {
            mxWorld.setLoaded(false);
        } else {
            Logger.logMessage(LogLevel.WARNING, Prefix.MXATLAS, "Could not unload MxWorld: " + mxWorld.getName());
        }
        return unloaded;
    }

    public boolean deleteMxWorld(MxWorld mxWorld) {
        if (mxWorld.isLoaded()) {
            if (!unloadMxWorld(mxWorld, false)) {
                return false;
            }
        }

        try {
            FileUtils.deleteDirectory(mxWorld.getDir());
        } catch (IOException e) {
            Logger.logMessage(LogLevel.WARNING, Prefix.MXATLAS, "Could not delete MxWorld: " + mxWorld.getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<MxWorld> duplicateMxWorld(MxWorld worldToClone, File dir) {
        UUID uuid = UUID.randomUUID();

        File directoryToCloneTo = new File(dir + File.separator + uuid);
        try {
            FileUtils.copyDirectory(worldToClone.getDir(), directoryToCloneTo);
            File uidDat = new File(directoryToCloneTo.getAbsoluteFile() + File.separator + "uid.dat");
            uidDat.delete();

        } catch (IOException e) {
            Logger.logMessage(LogLevel.WARNING, Prefix.MXATLAS, "Could not duplicate MxWorld: " + worldToClone.getName());
            e.printStackTrace();
            return Optional.empty();
        }
        MxWorld mxWorld = new MxWorld(uuid.toString(), uuid.toString(), directoryToCloneTo);
        worlds.add(mxWorld);

        return Optional.of(mxWorld);
    }

    public void unloadAll() {
        worlds.forEach(w -> {
            unloadMxWorld(w, true);
        });
    }

    public List<MxWorld> loadFolder(File dir) {
        List<MxWorld> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                File uidDat = new File(file.getAbsolutePath() + "/uid.dat");
                if (uidDat.exists()) {
                    MxWorld mxWorld = new MxWorld(file.getName(), file.getName(), file);
                    list.add(mxWorld);
                    worlds.add(mxWorld);
                    Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Adding world to MxAtlas: " + file.getName() + " (" + file.getAbsolutePath() + ")");
                } else {
                    Logger.logMessage(LogLevel.ERROR, Prefix.MXATLAS, "Could not load folder because it does not have a uid.dat file. (" + file.getAbsolutePath() + ")");
                }
            }
        }
        return list;
    }

    public Optional<MxWorld> loadWorld(File dir) {
        MxWorld mxWorld = new MxWorld(dir.getName(), dir.getName(), dir);
        worlds.add(mxWorld);
        Logger.logMessage(LogLevel.DEBUG, Prefix.MXATLAS, "Adding world to MxAtlas: " + dir.getName() + " (" + dir.getAbsolutePath() + ")");
        return Optional.of(mxWorld);
    }
}
