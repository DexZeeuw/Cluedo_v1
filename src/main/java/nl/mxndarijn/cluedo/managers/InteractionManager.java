package nl.mxndarijn.cluedo.managers;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.data.Interaction;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InteractionManager {

    private final File interactionFile;
    private final HashMap<Interaction, Boolean> interactions;

    public InteractionManager(File f) {
        this.interactionFile = f;
        interactions = new HashMap<>();
        if (!this.interactionFile.exists()) {
            try {
                this.interactionFile.createNewFile();
                for (Interaction value : Interaction.values()) {
                    interactions.put(value, value.isDefaultValue());
                    save();
                }
            } catch (IOException e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not create interaction file (" + f.getAbsolutePath() + ")");
                e.printStackTrace();
            }
        } else {
            loadInteractions();
        }
    }

    public void loadInteractions() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(interactionFile);
        fc.getKeys(false).forEach(key -> {
            interactions.put(Interaction.valueOf(key), fc.getBoolean(key));
        });
        for (Interaction value : Interaction.values()) {
            if (!interactions.containsKey(value)) {
                interactions.put(value, value.isDefaultValue());
            }
        }
    }

    public void save() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(interactionFile);
        interactions.forEach((k, v) -> {
            fc.set(k.toString(), v);
        });
        try {
            fc.save(interactionFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not save file. (" + interactionFile.getAbsolutePath() + ")");
            e.printStackTrace();
        }
    }

    public void setInteraction(Interaction i, boolean value) {
        interactions.put(i, value);
        save();
    }

    public File getInteractionFile() {
        return interactionFile;
    }

    public HashMap<Interaction, Boolean> getInteractions() {
        return interactions;
    }


    public boolean isInteractionWithTypeAllowed(Material type) {
        for (Map.Entry<Interaction, Boolean> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();
            Boolean value = entry.getValue();
            if (interaction.getMat() == type) {
                return value;
            }
        }
        return true;
    }
}
