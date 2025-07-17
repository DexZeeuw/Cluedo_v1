package nl.mxndarijn.cluedo.items.game.spectate;

import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeleportItem extends MxItem {
    public TeleportItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
            Optional<GamePlayer> oGp = game.getGamePlayerOfPlayer(p.getUniqueId());
            if(oGp.isEmpty()) {
                if (!game.getSpectators().contains(e.getPlayer().getUniqueId())) {
                    return;
                }
            } else {
                if(oGp.get().isAlive()) {
                    return;
                }
            }
            List<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
            optionalGame.get().getColors().forEach(gp -> {
                if (gp.getPlayer().isPresent() && gp.isAlive()) {
                    Player pl = Bukkit.getPlayer(gp.getPlayer().get());
                    if (pl != null) {
                        list.add(new Pair<>(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData(gp.getPlayer().get().toString())
                                .setName(ChatColor.GRAY + pl.getName())
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Kleur: " + gp.getMapPlayer().getColor().getDisplayName())
                                .build(),
                                (mxInv, e1) -> {
                                    p.teleport(pl.getLocation());
                                    p.closeInventory();
                                }));
                    }
                }
            });
            if (list.isEmpty()) {
                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_TELEPORT_NO_PLAYERS));
                return;
            }
            MxInventoryManager.getInstance().addAndOpenInventory(p, new MxListInventoryBuilder(ChatColor.GRAY + "Teleporteer naar speler", MxInventorySlots.THREE_ROWS)
                    .setListItems(list)
                    .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_THREE)
                    .build());

        }
    }
}
