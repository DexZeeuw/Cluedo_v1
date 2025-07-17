package nl.mxndarijn.cluedo.items.game.spectate;

import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Optional;

public class LeaveGameItem extends MxItem {
    public LeaveGameItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());
        if (optionalGame.isPresent()) {
            if (optionalGame.get().getSpectators().contains(p.getUniqueId())) {
                optionalGame.get().removeSpectator(p.getUniqueId());
                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_LEAVE));
                optionalGame.get().sendMessageToHosts(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_LEFT, Collections.singletonList(p.getName())));
            }
        }
    }
}
