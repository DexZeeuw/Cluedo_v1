package nl.mxndarijn.cluedo.items.game;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.game.UpcomingGameStatus;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameSpelerTool extends MxItem {

    public GameSpelerTool(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {

        Optional<Game> mapOptional = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Game game = mapOptional.get();
        if(game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;

        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (gp.isEmpty())
            return;

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Speler Tool", MxInventorySlots.THREE_ROWS)
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setName(ChatColor.GRAY + "Stel een vraag")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om een vraag te stellen aan een host.")
                                .setSkinFromHeadsData("message-icon")
                                .build(),
                        11,
                        (mxInv, e1) -> {
                            p.closeInventory();
                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ENTER_QUESTION));
                            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                game.sendMessageToHosts(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_MESSAGE_HOST, Arrays.asList(p.getName(), message)));
                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_QUESTION_SEND));
                            });
                        })
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setName(ChatColor.GRAY + "Stemmen")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om te stemmen.")
                                .setSkinFromHeadsData("book")
                                .build(),
                        13,
                        (mxInv, e1) -> {
                            List<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
                            game.getColors().forEach(gamePlayer -> {
                                if (gamePlayer.getPlayer().isPresent()) {
                                    if (gamePlayer.getPlayer().get().equals(p.getUniqueId()))
                                        return;
                                    if(!gamePlayer.isAlive())
                                        return;
                                    OfflinePlayer pl = Bukkit.getOfflinePlayer(gamePlayer.getPlayer().get());
                                    list.add(new Pair<>(
                                            MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData(pl.getUniqueId() + "")
                                                    .setName(ChatColor.GRAY + pl.getName())
                                                    .addLore(ChatColor.GRAY + "Kleur: " + gamePlayer.getMapPlayer().getColor().getDisplayName())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om op deze kleur te stemmen.")
                                                    .build(),
                                            (mxInv12, e22) -> {
                                                p.closeInventory();
                                                if(gp.get().getVotedOn().isPresent() && gp.get().getVotedOn().get() == gamePlayer) {
                                                    p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_VOTED, Arrays.asList(pl.getName(), gamePlayer.getMapPlayer().getColor().getDisplayName())));
                                                    return;
                                                }
                                                gp.get().setVotedOn(Optional.of(gamePlayer));
                                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_VOTED, Arrays.asList(pl.getName(), gamePlayer.getMapPlayer().getColor().getDisplayName())));
                                                game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_VOTED, Arrays.asList(p.getName(), gp.get().getMapPlayer().getColor().getDisplayName(), game.getTotalVotes() + "", ""+game.getColors().size())));
                                            }
                                    ));
                                }
                            });
                            MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Stemmen", MxInventorySlots.THREE_ROWS)
                                    .setListItems(list)
                                    .setPrevious(mxInv)
                                    .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                                    .setShowPageNumbers(false)
                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("message-icon")
                                                    .setName(ChatColor.GRAY + "Laat resultaten zien")
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om de resultaten aan")
                                                    .addLore(ChatColor.YELLOW + "iedereen te laten zien.")
                                                    .build(),
                                            18, (mxInv1, e2) -> {
                                                p.closeInventory();
                                                if (game.isPlayersCanEndVote()) {
                                                    game.showVotingResults(p.getName());
                                                } else {
                                                    p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_DISABLED_VOTE));
                                                }
                                            }
                                    )
                                    .build()
                            );
                        })
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setName(ChatColor.GRAY + "Kleuren")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om alle kleuren te zien.")
                                .setSkinFromHeadsData("color-block")
                                .build(),
                        15,
                        (mxInv, e1) -> {
                            p.closeInventory();
                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_COLOR_INFORMATION));
                            game.getColors().forEach(gamePlayer -> {
                                String name = gamePlayer.getPlayer().isPresent() ? Bukkit.getOfflinePlayer(gamePlayer.getPlayer().get()).getName() : "Niemand";
                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_COLOR_INFORMATION_PIECE, Arrays.asList(name, gamePlayer.getMapPlayer().getColor().getDisplayName(), gamePlayer.isAlive() ? ChatColor.GREEN + "Levend" : ChatColor.RED + "Dood")));
                            });
                        })
                .build());

    }
}
