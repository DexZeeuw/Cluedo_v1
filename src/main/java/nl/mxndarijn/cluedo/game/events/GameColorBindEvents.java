package nl.mxndarijn.cluedo.game.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import nl.mxndarijn.cluedo.data.ItemTag;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameColorBindEvents extends GameEvent{
    public GameColorBindEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void armorEquip(PlayerArmorChangeEvent e) {
        if(!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if(gp.isEmpty())
            return;

        ItemStack armorContent =  e.getNewItem();
        if(armorContent == null)
            return;
        if(armorContent.getItemMeta() == null)
            return;
        String data = armorContent.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.COLORBIND.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        ItemStack is = armorContent.clone();
        List<String> colors = Arrays.asList(data.split(";"));
        if(!colors.contains(gp.get().getMapPlayer().getColor().getType())) {
            e.getPlayer().getInventory().addItem(is);
            switch (e.getSlotType()) {
                case HEAD -> e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
                case CHEST -> e.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
                case LEGS -> e.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
                case FEET -> e.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent e) {
        if(!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if(gp.isEmpty())
            return;

        ItemStack is = e.getPlayer().getInventory().getItemInMainHand();

        if(is == null)
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.COLORBIND.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        List<String> colors = Arrays.asList(data.split(";"));
        if(!colors.contains(gp.get().getMapPlayer().getColor().getType())) {
            e.setCancelled(true);
            e.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(EntityDamageByEntityEvent e) {
        if(!validateWorld(e.getDamager().getWorld()))
            return;
        if(!(e.getDamager() instanceof Player))
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getDamager().getUniqueId());
        if(gp.isEmpty())
            return;

        ItemStack is = ((Player) e.getDamager()).getInventory().getItemInMainHand();

        if(is == null)
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.COLORBIND.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        List<String> colors = Arrays.asList(data.split(";"));
        if(!colors.contains(gp.get().getMapPlayer().getColor().getType())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void move(InventoryClickEvent e) {
        ItemStack is = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        if (player == null) {
            return;
        }
        if(is == null)
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(player.getUniqueId());
        if(gp.isEmpty() && !game.getSpectators().contains(player.getUniqueId()))
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.DROPPABLE.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        if(e.getClickedInventory() == null)
            return;
        if(data.equalsIgnoreCase("false")) {
            InventoryAction a = e.getAction();
            if(e.getClickedInventory().equals(player.getInventory())) {
                if(a == InventoryAction.MOVE_TO_OTHER_INVENTORY || a == InventoryAction.SWAP_WITH_CURSOR || a == InventoryAction.HOTBAR_SWAP)
                    e.setCancelled(true);
                if(player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING && player.getOpenInventory().getBottomInventory() == player.getInventory()) {
                    e.setCancelled(true);
                }
            } else {
                if (a != InventoryAction.PICKUP_ALL && a != InventoryAction.PICKUP_HALF && a != InventoryAction.PICKUP_ONE && a != InventoryAction.PICKUP_SOME) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void hotbarSwap(InventoryClickEvent e) {
        if(e.getHotbarButton() == -1)
            return;
        ItemStack is = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
        Player player = (Player) e.getWhoClicked();
        if (player == null) {
            return;
        }
        if(is == null)
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(player.getUniqueId());
        if(gp.isEmpty() && !game.getSpectators().contains(player.getUniqueId()))
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.DROPPABLE.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        if(e.getClickedInventory() == null)
            return;
        if(data.equalsIgnoreCase("false")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void moveCursor(InventoryClickEvent e) {
        ItemStack is = e.getCursor();
        Player player = (Player) e.getWhoClicked();
        if(is == null)
            return;
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(player.getUniqueId());
        if(gp.isEmpty() && !game.getSpectators().contains(player.getUniqueId()))
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.DROPPABLE.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        if(data.equalsIgnoreCase("false")) {
            InventoryAction a = e.getAction();
            if(e.getClickedInventory() != null && !e.getClickedInventory().equals(player.getInventory())) {
                if (a != InventoryAction.PICKUP_ALL && a != InventoryAction.PICKUP_HALF && a != InventoryAction.PICKUP_ONE && a != InventoryAction.PICKUP_SOME) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void drop(PlayerDropItemEvent e) {
        ItemStack is = e.getItemDrop().getItemStack();
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }
        if(is == null)
            return;

        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(player.getUniqueId());
        if(gp.isEmpty() && !game.getSpectators().contains(player.getUniqueId()))
            return;
        if(is.getItemMeta() == null)
            return;
        String data = is.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.DROPPABLE.getPersistentDataTag()), PersistentDataType.STRING);
        if(data == null)
            return;
        if(data.equalsIgnoreCase("false")) {
            e.setCancelled(true);
        }
    }



}
