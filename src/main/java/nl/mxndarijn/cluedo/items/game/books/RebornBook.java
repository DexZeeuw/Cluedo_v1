package nl.mxndarijn.cluedo.items.game.books;

import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.data.AvailablePerson;
import nl.mxndarijn.cluedo.data.BookFailurePlayersHolder;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.game.UpcomingGameStatus;
import nl.mxndarijn.cluedo.items.Items;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RebornBook extends Book {
    public RebornBook(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        getGame(p.getWorld());
        if (game == null)
            return;

        Optional<GamePlayer> optionalGamePlayer = getGamePlayer(p.getUniqueId());

        if (optionalGamePlayer.isPresent()) {
            if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
                return;
            GamePlayer gp = optionalGamePlayer.get();
            if (!gp.isAlive())
                return;
            List<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
            game.getColors().forEach(gamePlayer -> {
                if (gamePlayer.getPlayer().isEmpty())
                    return;
                if (gamePlayer.isAlive())
                    return;
                if (gamePlayer.getPlayer().get().equals(p.getUniqueId()))
                    return;
                if (gamePlayer.isPeacekeeperChestOpened() && gamePlayer.getMapPlayer().isPeacekeeper())
                    return;
                Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                if (player == null)
                    return;

                list.add(new Pair<>(
                        MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData(player.getUniqueId().toString())
                                .setName(ChatColor.GRAY + player.getName())
                                .addLore(gamePlayer.getMapPlayer().getColor().getDisplayName())
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om " + player.getName())
                                .addLore(ChatColor.YELLOW + " te rebornen. (genezen)")
                                .build(),
                        (mxInv, e1) -> {
                            p.closeInventory();
                            for (Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(is.getType()).entrySet()) {
                                Integer key = entry.getKey();
                                ItemStack value = entry.getValue();
                                if (isItemTheSame(value)) {
                                    if(!canItemExecute(p, key, value, BookFailurePlayersHolder.create().setData(AvailablePerson.EXECUTOR, p)))
                                        return;
                                    gamePlayer.setAlive(true);
                                    game.removeSpectator(player.getUniqueId(), false);
                                    player.getInventory().clear();
                                    player.closeInventory();
                                    player.setAllowFlight(false);
                                    player.teleport(p.getLocation());
                                    player.getInventory().addItem(Items.GAME_PLAYER_TOOL.getItemStack());

                                    // Check if book is silenced
                                    if (isSilenced(value)) {
                                        game.sendMessageToHosts(ChatColor.translateAlternateColorCodes('&', String.format("&7&o[SILENT] &f%s", LanguageManager.getInstance().getLanguageString(LanguageText.GAME_REBORN_MESSAGE, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player.getName())))));
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7&o[SILENT] &f%s", LanguageManager.getInstance().getLanguageString(LanguageText.GAME_REBORN_MESSAGE, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player.getName())))));
                                    } else {
                                        sendBookMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_REBORN_MESSAGE, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player.getName())));
                                    }
                                    break;
                                }
                            }
                        }
                ));
            });

            MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Reborn", MxInventorySlots.SIX_ROWS)
                    .setAvailableSlots(12, 13, 14, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 41, 42, 43)
                    .setShowPageNumbers(false)
                    .setListItems(list)
                    .build());


        }
    }
}
