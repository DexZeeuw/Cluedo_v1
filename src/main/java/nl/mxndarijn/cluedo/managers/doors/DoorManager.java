package nl.mxndarijn.cluedo.managers.doors;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.mxworld.MxWorld;
import nl.mxndarijn.cluedo.map.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoorManager {

    private final File doorFile;
    private final List<DoorInformation> doors;

    public DoorManager(File f) {
        this.doorFile = f;
        if (!this.doorFile.exists()) {
            try {
                this.doorFile.createNewFile();
            } catch (IOException e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not create door file (" + f.getAbsolutePath() + ")");
                e.printStackTrace();
            }
        }
        doors = loadFile();
    }

    private List<DoorInformation> loadFile() {
        ArrayList<DoorInformation> list = new ArrayList<>();
        FileConfiguration fc = YamlConfiguration.loadConfiguration(doorFile);
        fc.getKeys(false).forEach(key -> {
            Optional<DoorInformation> optionalChestInformation = DoorInformation.load(fc.getConfigurationSection(key));
            optionalChestInformation.ifPresent(list::add);
        });

        return list;
    }

    public void save() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(doorFile);
        fc.getKeys(false).forEach(k -> {
            fc.set(k, null);
        });
        doors.forEach(w -> {
            w.save(fc);
        });
        try {
            fc.save(doorFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not save file. (" + doorFile.getAbsolutePath() + ")");
            e.printStackTrace();
        }
    }

    public void addDoor(DoorInformation information) {
        doors.add(information);
        save();
    }

    public void removeDoor(DoorInformation information) {
        doors.remove(information);
        save();
    }


    public Optional<DoorInformation> getDoorById(String id) {
        for (DoorInformation door : doors) {
            if (door.getUuid().equalsIgnoreCase(id)) {
                return Optional.of(door);
            }
        }
        return Optional.empty();
    }

    public List<DoorInformation> getDoors() {
        return doors;
    }

    public boolean areAllDoorsClosed(Map map) {
        Optional<MxWorld> world = map.getMxWorld();
        if (world.isEmpty())
            return false;
        MxWorld m = world.get();
        World w = Bukkit.getWorld(m.getWorldUID());
        for (DoorInformation door : doors) {
            boolean foundDoor = !door.getLocations().isEmpty();
            if (foundDoor) {
                MxLocation inf = door.getLocations().keySet().iterator().next();
                Location loc = inf.getLocation(w);
                Block placedBlock = loc.getBlock();
                if (placedBlock.getType() == Material.AIR) {
                    return false;
                }
            }
        }
        return true;
    }
}
