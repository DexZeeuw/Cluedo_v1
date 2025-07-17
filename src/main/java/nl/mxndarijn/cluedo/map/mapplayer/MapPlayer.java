package nl.mxndarijn.cluedo.map.mapplayer;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.cluedo.data.Colors;
import nl.mxndarijn.cluedo.data.Role;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class MapPlayer {
    private final Colors color;
    private MxLocation location;
    private Role role;
    private boolean isPeacekeeper;

    public MapPlayer(Colors color, MxLocation location) {
        this.color = color;
        this.location = location;
        this.role = Role.SPELER;
        this.isPeacekeeper = false;
    }

    public MapPlayer(Colors color, MxLocation location, Role role, boolean isPeacekeeper) {
        this.color = color;
        this.location = location;
        this.role = role;
        this.isPeacekeeper = isPeacekeeper;
    }

    public static Optional<MapPlayer> loadMapPlayerFromConfigurationSection(ConfigurationSection section) {
        Optional<Colors> color = Colors.getColorByType(section.getName());
        if (color.isEmpty()) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load color");
            return Optional.empty();
        }
        ConfigurationSection locationSection = section.getConfigurationSection(MapPlayerConfigValue.LOCATION.getConfigValue());

        if (locationSection == null) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load spawnpoint for color: (Section null) " + color.get().getType() + " Path: " + section.getCurrentPath());
            return Optional.empty();
        }

        boolean isPeacekeeper = section.getBoolean(MapPlayerConfigValue.IS_PEACEKEEPER.getConfigValue(), false);
        Optional<Role> role = Role.getRoleByType(section.getString(MapPlayerConfigValue.ROLE.getConfigValue(), Role.SPELER.getRoleType()));
        if (role.isEmpty()) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load role for color: " + color.get().getType());
            return Optional.empty();
        }

        Optional<MxLocation> optionalMxLocation = MxLocation.loadFromConfigurationSection(locationSection);

        if (optionalMxLocation.isEmpty()) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load spawnpoint for color: " + color.get().getType());
            return Optional.empty();
        }

        MxLocation mxLocation = optionalMxLocation.get();
        return Optional.of(new MapPlayer(color.get(), mxLocation, role.get(), isPeacekeeper));
    }

    public void save(ConfigurationSection section) {
        ConfigurationSection colorSection = section.createSection(color.getType());

        ConfigurationSection locationSection = colorSection.createSection(MapPlayerConfigValue.LOCATION.getConfigValue());
        location.write(locationSection);
        colorSection.set(MapPlayerConfigValue.ROLE.getConfigValue(), role.getRoleType());
        colorSection.set(MapPlayerConfigValue.IS_PEACEKEEPER.getConfigValue(), isPeacekeeper);
    }

    public Colors getColor() {
        return color;
    }

    public MxLocation getLocation() {
        return location;
    }

    public void setLocation(MxLocation location) {
        this.location = location;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isPeacekeeper() {
        return isPeacekeeper;
    }

    public void setPeacekeeper(boolean peacekeeper) {
        isPeacekeeper = peacekeeper;
    }

    public String getRoleDisplayString() {
        return isPeacekeeper ? role.getPeacekeeperName() : role.getRolName();
    }

    public String getRoleDisplayWithoutPeacekeeper() {
        return role.getRolName();
    }
}
