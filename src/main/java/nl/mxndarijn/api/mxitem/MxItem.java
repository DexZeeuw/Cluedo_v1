package nl.mxndarijn.api.mxitem;

import nl.mxndarijn.api.inventory.saver.InventoryManager;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.ItemTag;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class MxItem implements Listener {

    public final JavaPlugin plugin;
    private final ItemStack is;
    private final MxWorldFilter worldFilter;
    private final LanguageManager languageManager;
    private final boolean gameItem;
    private final Action[] actions;


    public MxItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        this.is = is;
        this.worldFilter = worldFilter;
        this.gameItem = gameItem;
        this.languageManager = LanguageManager.getInstance();
        this.actions = actions;

        plugin = JavaPlugin.getPlugin(Cluedo.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (Arrays.stream(actions).noneMatch(a -> a == e.getAction())) {
            return;
        }

        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (e.getItem() == null || !e.getItem().hasItemMeta() || e.getItem().getType() == Material.AIR) {
            return;
        }
        Player p = e.getPlayer();
        if (gameItem) {
            if (!GameWorldManager.getInstance().isPlayerPLayingInAGame(p.getUniqueId())) {
                return;
            }
        }

        if (!InventoryManager.validateItem(e.getItem(), is)) {
            return;
        }

        if(checkColorBind(e)) {
            return;
        }


        try {
            execute(p, e);
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MXITEM, "Could not execute item: " + Functions.convertComponentToString(is.getItemMeta().displayName()));
            ex.printStackTrace();
            p.sendMessage(languageManager.getLanguageString(LanguageText.ERROR_WHILE_EXECUTING_ITEM, Collections.emptyList(), ChatPrefix.WIDM));
        }

    }

    private boolean checkColorBind(PlayerInteractEvent e) {
        Optional<Game> game = GameWorldManager.getInstance().getGameByWorldUID(e.getPlayer().getWorld().getUID());
        if(game.isPresent()) {
            Optional<GamePlayer> gp = game.get().getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
            if(gp.isPresent()) {
                ItemStack is = e.getItem();
                if(is == null)
                    return false;
                if(is.getItemMeta() == null)
                    return false;

                String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.COLORBIND.getPersistentDataTag()), PersistentDataType.STRING);
                if(data == null)
                    return false;
                List<String> colors = Arrays.asList(data.split(";"));
                if(!colors.contains(gp.get().getMapPlayer().getColor().getType())) {
                    e.setCancelled(true);
                    return true;
                }
            }
        }
        return false;
    }

    public abstract void execute(Player p, PlayerInteractEvent e);

//    @EventHandler
//    public void interactEntity(PlayerInteractEntityEvent e) {
//        if(e.get)
//
//    }

//    @EventHandler
//    public void interactAtEntity(PlayerInteractAtEntityEvent e) {
//
//    }


}
