package nl.mxndarijn.cluedo.items.game;

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
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.shulkers.ShulkerInformation;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class GameShulkerItem extends MxItem {


    public GameShulkerItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Game> mapOptional = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Game game = mapOptional.get();

        if (!game.getHosts().contains(p.getUniqueId()))
            return;

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        game.getShulkerManager().getShulkers().forEach(shulker -> {
            list.add(new Pair<>(
                    MxDefaultItemStackBuilder.create(shulker.getMaterial(), 1)
                            .setName(ChatColor.GRAY + shulker.getName())
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Location: " + shulker.getLocation().getX() + " " + shulker.getLocation().getY() + " " + shulker.getLocation().getZ())
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Beginkist: " + (shulker.isStartingRoom() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee"))
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik om de shulker op afstand te openen.")
                            .build(),
                    (mxInv, e12) -> {

                        World w = Bukkit.getWorld(game.getMxWorld().get().getWorldUID());
                        Location loc = shulker.getLocation().getLocation(w);
                        if (game.getMxWorld().isEmpty()) {
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
    public void interactEvent(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }

        Player p = e.getPlayer();
        Optional<Game> mapOptional = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Game game = mapOptional.get();
        Optional<ShulkerInformation> optionalShulkerInformation = game.getShulkerManager().getShulkerByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
        if (optionalShulkerInformation.isEmpty()) {
            return;
        }

        ShulkerInformation shulkerInformation = optionalShulkerInformation.get();

        Optional<Colors> c = Colors.getColorByMaterial(shulkerInformation.getMaterial());
        if (c.isEmpty())
            return;

        Optional<MapPlayer> mp = game.getConfig().getMapPlayerOfColor(c.get());
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
