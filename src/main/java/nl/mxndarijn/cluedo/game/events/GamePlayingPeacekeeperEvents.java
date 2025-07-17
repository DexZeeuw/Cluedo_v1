package nl.mxndarijn.cluedo.game.events;

import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.game.UpcomingGameStatus;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class GamePlayingPeacekeeperEvents extends GameEvent {
    public GamePlayingPeacekeeperEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler
    public void peacekeeperOpenChest(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof Chest) && !(e.getClickedBlock().getState() instanceof Dropper) && !(e.getClickedBlock().getState() instanceof Dispenser) && !(e.getClickedBlock().getState() instanceof Hopper)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (optionalGamePlayer.isEmpty())
            return;
        if (optionalGamePlayer.get().isPeacekeeperChestOpened() && optionalGamePlayer.get().getMapPlayer().isPeacekeeper())
            e.setCancelled(true);
    }

    @EventHandler
    public void itemDropPeacekeeper(PlayerDropItemEvent e) {
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        if (e.getItemDrop().getItemStack().getItemMeta() == null || e.getItemDrop().getItemStack().lore() == null) {
            return;
        }
        e.getItemDrop().getItemStack().lore().forEach(l -> {

            if (Functions.convertComponentToString(l).equalsIgnoreCase((ChatColor.GOLD + "Peacekeeper-Item"))) {
                e.setCancelled(true);
            }
        });
    }
}
