package nl.mxndarijn.cluedo.game.events;

import nl.mxndarijn.cluedo.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public abstract class GameEvent implements Listener {

    public Game game;
    public JavaPlugin plugin;

    public GameEvent(Game g, JavaPlugin plugin) {
        this.game = g;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public boolean validateWorld(World w) {
        return game.getMxWorld().isPresent() && w.getUID().equals(game.getMxWorld().get().getWorldUID());
    }
}
