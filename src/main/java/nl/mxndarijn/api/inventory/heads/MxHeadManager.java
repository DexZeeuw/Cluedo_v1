package nl.mxndarijn.api.inventory.heads;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.data.ConfigFiles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class MxHeadManager {
    private static MxHeadManager instance;
    private final FileConfiguration fileConfiguration;

    public MxHeadManager() {
        fileConfiguration = ConfigFiles.HEAD_DATA.getFileConfiguration();
        Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(Cluedo.class), () -> {
            Logger.logMessage(LogLevel.INFORMATION, Prefix.MXHEAD_MANAGER, "Refreshing player skulls...");
            for (String key : fileConfiguration.getKeys(false)) {
                Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(key);
                if (optionalSection.isPresent()) {
                    MxHeadSection section = optionalSection.get();
                    if (section.getType().get() == MxHeadsType.PLAYER) {
                        Logger.logMessage(LogLevel.DEBUG, Prefix.MXHEAD_MANAGER, "Refreshing skull: " + key);
                        Optional<String> value = getTexture(section.getUuid().get());
                        if (!value.isPresent()) {
                            Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not get texture for " + key + ", skipping texture...");
                            continue;
                        }
                        if (!section.getValue().get().equalsIgnoreCase(value.get())) {
                            section.setValue(value.get());
                            section.apply();
                        }
                    }
                }
            }
        });
    }

    public static MxHeadManager getInstance() {
        if (instance == null) {
            instance = new MxHeadManager();
        }
        return instance;
    }

    public Optional<String> getTextureValue(String name) {
        Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(name);
        if (optionalSection.isPresent()) {
            MxHeadSection section = optionalSection.get();
            return section.getValue();
        }
        return Optional.empty();
    }

    public List<String> getAllHeadKeys() {
        return new ArrayList<>(fileConfiguration.getKeys(false));
    }

    public boolean storeSkullTexture(ItemStack itemStack, String textureName, String displayName, MxHeadsType type) {
        Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(textureName);
        Optional<UUID> ownerOptional = Optional.empty();
        if (type == MxHeadsType.PLAYER) {
            ownerOptional = getOwner(itemStack);
        }
        Optional<String> optionalTexture = Optional.empty();
        if (type == MxHeadsType.PLAYER) {
            if (ownerOptional.isPresent()) {
                optionalTexture = getTexture(ownerOptional.get());
            }
        } else {
            optionalTexture = getTextureValue(itemStack);
        }
        if (optionalTexture.isPresent()) {
            String texture = optionalTexture.get();
            if (optionalSection.isPresent()) {
                MxHeadSection section = optionalSection.get();
                section.setType(type);
                section.setValue(texture);
                section.setName(displayName);
                ownerOptional.ifPresent(section::setUuid);
                section.apply();
                return true;
            } else {
                if (type == MxHeadsType.PLAYER) {
                    if (ownerOptional.isPresent()) {
                        Optional<MxHeadSection> section = MxHeadSection.create(textureName, displayName, type, texture, ownerOptional.get());
                        if (!section.isPresent()) {
                            Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
                            return false;
                        }
                        section.get().apply();
                        return true;
                    } else {
                        Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Error while creating head-data, no owner but type is player.");
                        return false;
                    }
                } else {
                    Optional<MxHeadSection> section = MxHeadSection.create(textureName, displayName, type, texture);
                    if (!section.isPresent()) {
                        Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
                        return false;
                    }
                    section.get().apply();
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<String> getTextureValue(ItemStack itemStack) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            try {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                GameProfile profile = (GameProfile) profileField.get(skullMeta);
                Collection<Property> textures = profile.getProperties().get("textures");

                Optional<Property> optionalTexture = textures.stream().findFirst();
                if (optionalTexture.isPresent()) {
                    Property texture = optionalTexture.get();
                    return Optional.of(texture.getValue());
                } else {
                    Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not find texture");
                    return Optional.empty();
                }
            } catch (Exception e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Error while retrieving texture:");
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    private Optional<UUID> getOwner(ItemStack itemStack) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            OfflinePlayer player = skullMeta.getOwningPlayer();
            if (player == null) {
                Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not load head data, because it does not have an owner.");
                return Optional.empty();
            }
            return Optional.of(skullMeta.getOwningPlayer().getUniqueId());
        } else {
            Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    public void removeHead(String key) {
        if (fileConfiguration.contains(key)) {
            fileConfiguration.set(key, null);
        }
    }

    public Optional<MxHeadSection> getHeadSection(String key) {
        return MxHeadSection.loadHead(key);
    }

    private Optional<String> getTexture(UUID uuid) {
        Optional<String> texture = Optional.empty();
        try {
            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            texture = Optional.of(textureProperty.get("value").getAsString());
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MXHEAD_MANAGER, "Could not retrieve skin data from mojang servers... (" + uuid.toString() + ")");
            e.printStackTrace();
        }
        return texture;
    }
}
