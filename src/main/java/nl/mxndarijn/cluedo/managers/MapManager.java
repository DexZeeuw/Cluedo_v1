package nl.mxndarijn.cluedo.managers;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxworld.MxWorld;
import nl.mxndarijn.cluedo.data.SpecialDirectories;
import nl.mxndarijn.cluedo.map.Map;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class MapManager {
    private static MapManager instance;

    private final ArrayList<Map> maps;

    private MapManager() {
        maps = new ArrayList<>();
        Arrays.stream(SpecialDirectories.MAP_WORLDS.getDirectory().listFiles()).forEach(file -> {
            if (file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(mapFile -> {
                    Optional<Map> optionalMap = Map.create(mapFile);
                    if (optionalMap.isPresent()) {
                        Logger.logMessage(LogLevel.INFORMATION, Prefix.MAPS_MANAGER, "Map Added: " + mapFile.getName());
                        maps.add(optionalMap.get());
                    } else {
                        Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load map: " + mapFile.getName());
                    }
                });
            }
        });
    }

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    public ArrayList<Map> getAllMaps() {
        return maps;
    }

    public Optional<Map> getMapById(String s) {
        for (Map m : maps) {
            if (m.getDirectory().getName().equals(s)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public Optional<Map> getMapByWorldUID(UUID uid) {
        for (Map m : maps) {
            if (m.getMxWorld().isPresent()) {
                MxWorld mxWorld = m.getMxWorld().get();
                if (mxWorld.isLoaded()) {
                    if (mxWorld.getWorldUID() == uid) {
                        return Optional.of(m);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public void addMap(Map map) {
        maps.add(map);
    }

    public void removeMap(Map map) {
        try {
            FileUtils.deleteDirectory(map.getDirectory());
            if (map.getDirectory().exists())
                FileUtils.forceDelete(map.getDirectory());
            maps.remove(map);

        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, "Could not delete map " + map.getDirectory().getAbsolutePath());
            e.printStackTrace();
        }
    }
}
