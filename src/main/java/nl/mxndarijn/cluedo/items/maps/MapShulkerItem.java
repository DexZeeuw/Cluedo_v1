package nl.mxndarijn.cluedo.items.maps;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.Colors;
import nl.mxndarijn.cluedo.data.CustomInventoryOverlay;
import nl.mxndarijn.cluedo.managers.MapManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.shulkers.ShulkerInformation;
import nl.mxndarijn.cluedo.map.Map;
import nl.mxndarijn.cluedo.map.mapplayer.MapPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapShulkerItem extends MxItem {


    public MapShulkerItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        map.getShulkerManager().getShulkers().forEach(shulker -> {
            list.add(new Pair<>(
                    MxDefaultItemStackBuilder.create(shulker.getMaterial(), 1)
                            .setName(ChatColor.GRAY + shulker.getName())
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Location: " + shulker.getLocation().getX() + " " + shulker.getLocation().getY() + " " + shulker.getLocation().getZ())
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Beginkist: " + (shulker.isStartingRoom() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee"))
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik om de shulker op afstand te openen.")
                            .addLore(ChatColor.YELLOW + "Shift-Klik om de shulker wel of geen beginkist te maken (togglen).")
                            .build(),
                    (mxInv, e12) -> {
                        if (e12.isShiftClick()) {
                            shulker.setStartingRoom(!shulker.isStartingRoom());
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.SHULKER_TOOL_TOGGLED_BEGINKIST, Collections.singletonList(shulker.isStartingRoom() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee")));
                            p.closeInventory();
                            return;
                        }

                        World w = Bukkit.getWorld(map.getMxWorld().get().getWorldUID());
                        Location loc = shulker.getLocation().getLocation(w);
                        if (map.getMxWorld().isEmpty()) {
                            return;
                        }
                        Block block = loc.getBlock();
                        if (block.getState() instanceof ShulkerBox shulkerBox) {
                            p.openInventory(shulkerBox.getInventory());
                        } else {
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_SHULKER_IS_NOT_A_SHULKER));
                            p.closeInventory();
                        }
                    }
            ));
        });


        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Shulker Hulp Tool", MxInventorySlots.SIX_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                .setListItems(list)
                .build());

    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (!(e.getBlockPlaced().getState() instanceof ShulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        AtomicBoolean bool = new AtomicBoolean(true);
        map.getShulkerManager().getShulkers().forEach(shulker -> {
            if (shulker.getMaterial() == e.getBlock().getType()) {
                bool.set(false);
            }
        });
        map.getShulkerManager().addShulker(new ShulkerInformation("Automatisch toegevoegde shulker", MxLocation.getFromLocation(e.getBlockPlaced().getLocation()), e.getBlock().getType(), bool.get()));
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_AUTOMATED_SHULKER_ADDED, Collections.singletonList(bool.get() ? "Ja" : "Nee")));
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof ShulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        Optional<ShulkerInformation> info = map.getShulkerManager().getShulkerByLocation(MxLocation.getFromLocation(e.getBlock().getLocation()));
        if (info.isEmpty()) {
            return;
        }

        map.getShulkerManager().removeShulker(info.get());
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_AUTOMATED_SHULKER_REMOVED));
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }

        Player p = e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        Optional<ShulkerInformation> optionalShulkerInformation = map.getShulkerManager().getShulkerByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
        if (optionalShulkerInformation.isEmpty()) {
            return;
        }

        ShulkerInformation shulkerInformation = optionalShulkerInformation.get();

        Optional<Colors> c = Colors.getColorByMaterial(shulkerInformation.getMaterial());
        if (c.isEmpty())
            return;

        Optional<MapPlayer> mp = map.getMapConfig().getMapPlayerOfColor(c.get());
        if (mp.isEmpty())
            return;

        String title = mp.get().getRole().getUnicode();
        if (shulkerInformation.isStartingRoom()) {
            shulkerBox.customName(Component.text(title));
        } else {
            if (mp.get().isPeacekeeper()) {
                shulkerBox.customName(Component.text(CustomInventoryOverlay.ROLES_PEACEKEEPER.getUnicodeCharacter()));
            } else {
                shulkerBox.customName(null);
            }
        }
        shulkerBox.update();
        // change name of shulker inventory
    }

}
