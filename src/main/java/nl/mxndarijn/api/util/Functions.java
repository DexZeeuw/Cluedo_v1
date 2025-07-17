package nl.mxndarijn.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.Cluedo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class Functions {
    public static Location getSpawnLocation() {
        return Bukkit.getWorld("world").getSpawnLocation();
    }

    public static Location getLocationFromConfiguration(World w, ConfigurationSection section) {
        if (section.contains("yaw") && section.contains("pitch")) {
            return new Location(w, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        } else {
            return new Location(w, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
        }
    }

    public static void copyFileFromResources(String fileName, String path) {
        JavaPlugin plugin = JavaPlugin.getPlugin(Cluedo.class);
        File destFile = new File(plugin.getDataFolder() + File.separator + path);
        destFile.getParentFile().mkdirs();

        copyFileFromResources(fileName, destFile);
    }

    public static void copyFileFromResources(String fileName, File destFile) {
        destFile.getParentFile().mkdirs();
        JavaPlugin plugin = JavaPlugin.getPlugin(Cluedo.class);
        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream == null) {
            Logger.logMessage(LogLevel.FATAL, Prefix.CONFIG_FILES, "Could load resource: " + fileName);
            return;
        }

        try (OutputStream outputStream = Files.newOutputStream(destFile.toPath())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Logger.logMessage(LogLevel.FATAL, Prefix.CONFIG_FILES, "Could not create config file: " + fileName);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not close stream for config file: " + fileName);
            }
        }
    }

    public static String convertComponentToString(Component c) {
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.builder().build();
        return plainSerializer.serialize(c);
    }

    public static String formatGameTime(long timeInMillis) {
        // Converteer milliseconden naar seconden
        long timeInSeconds = timeInMillis / 1000;

        // Bereken het aantal minuten en seconden van de gegeven tijd
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;

        // Gebruik String.format om de tijd in het juiste formaat weer te geven
        return String.format("%02d:%02d", minutes, seconds);
    }

//    public static String convertWithColors(String input) {
//        return RGBUtils.getInstance().convertRGBtoLegacy(input);
//    }
//
//    public static List<String> convertListWithColors(List<String> inputList) {
//        return inputList.stream()
//                .map(Functions::convertWithColors)
//                .collect(Collectors.toList());
//    }
//
//    public static java.awt.Color hexToRgb(String hex) {
//        // Verwijder het '#' karakter als het aanwezig is
//        if (hex.startsWith("#")) {
//            hex = hex.substring(1);
//        }
//
//        // Controleer of de hexadecimale kleurcode geldig is
//        if (hex.length() != 6) {
//            throw new IllegalArgumentException("Ongeldige hexadecimale kleurcode");
//        }
//
//        // Parse de hexadecimale waarden voor rood, groen en blauw
//        int red = Integer.parseInt(hex.substring(0, 2), 16);
//        int green = Integer.parseInt(hex.substring(2, 4), 16);
//        int blue = Integer.parseInt(hex.substring(4, 6), 16);
//
//        // Maak een java.awt.Color object en retourneer het
//        return new java.awt.Color(red, green, blue);
//    }
}
