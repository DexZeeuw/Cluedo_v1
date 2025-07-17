package nl.mxndarijn.cluedo.managers.doors;

import nl.mxndarijn.api.mxworld.MxLocation;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class DoorInformation {
    private final HashMap<MxLocation, Material> locations;
    private String uuid;
    private String name;

    public DoorInformation(String name) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.locations = new HashMap<>();
    }

    private DoorInformation() {
        this.locations = new HashMap<>();
    }

    public static Optional<DoorInformation> load(ConfigurationSection section) {
        if (section == null) {
            return Optional.empty();
        }
        DoorInformation i = new DoorInformation();
        i.uuid = section.getName();
        i.name = section.getString("name");
        if (section.contains("locations")) {
            section.getMapList("locations").forEach(map -> {
                MxLocation location = new MxLocation();
                location.setX((double) map.get("x"));
                location.setY((double) map.get("y"));
                location.setZ((double) map.get("z"));
                Material mat = Material.matchMaterial((String) map.get("material"));
                i.locations.put(location, mat);
            });
        }
        return Optional.of(i);
    }

    public void save(FileConfiguration fc) {
        ConfigurationSection section = fc.createSection(uuid);
        section.set("name", name);
        List<Map<String, Object>> list = new ArrayList<>();
        locations.keySet().forEach(location -> {
            Map<String, Object> l = new HashMap<>();
            l.put("x", location.getX());
            l.put("y", location.getY());
            l.put("z", location.getZ());
            l.put("material", locations.get(location).toString());

            list.add(l);
        });
        section.set("locations", list);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public HashMap<MxLocation, Material> getLocations() {
        return locations;
    }

    public void addLocation(MxLocation location, Material mat) {
        locations.put(location, mat);
    }

    public void removeLocation(MxLocation location) {
        locations.remove(location);
    }

    public boolean containsLocation(MxLocation location) {
        for (MxLocation l : locations.keySet()) {
            if (l.equals(location)) {
                return true;
            }
        }
        return false;
    }

    public Optional<MxLocation> getLocation(MxLocation location) {
        for (MxLocation l : locations.keySet()) {
            if (l.equals(location)) {
                return Optional.of(l);
            }
        }
        return Optional.empty();
    }

    public void open(World w) {
        locations.forEach((loc, mat) -> {
            loc.getLocation(w).getBlock().setType(Material.AIR);
        });
    }

    public void close(World w) {
        locations.forEach((loc, mat) -> {
            loc.getLocation(w).getBlock().setType(mat);
        });
    }
}
