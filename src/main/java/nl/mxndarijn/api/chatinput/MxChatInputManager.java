package nl.mxndarijn.api.chatinput;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.Cluedo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class MxChatInputManager implements Listener {

    private static MxChatInputManager instance;
    private final HashMap<UUID, MxChatInputCallback> map;

    private MxChatInputManager() {
        map = new HashMap<>();
        JavaPlugin plugin = JavaPlugin.getPlugin(Cluedo.class);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Logger.logMessage(LogLevel.INFORMATION, Prefix.MXCHATINPUT_MANAGER, "MxChatInputManager loaded...");

    }

    public static MxChatInputManager getInstance() {
        if (instance == null) {
            instance = new MxChatInputManager();
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chatEvent(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (map.containsKey(uuid)) {
            e.setCancelled(true);
            MxChatInputCallback inputCallback = map.get(uuid);
            map.remove(uuid);
            inputCallback.textReceived(Functions.convertComponentToString(e.message()));
        }
    }

    public String addChatInputCallback(UUID uuid, MxChatInputCallback callback) {
        map.put(uuid, callback);
        return null;
    }
}
