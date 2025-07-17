package nl.mxndarijn.cluedo.items.game;

import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.managers.VanishManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


public class VanishItem extends MxItem {

    public VanishItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
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

        VanishManager.getInstance().toggleVanish(p);
        if (VanishManager.getInstance().isPlayerHidden(p)) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.VANISH_ON, ChatPrefix.WIDM));
        } else {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.VANISH_OFF, ChatPrefix.WIDM));
        }
    }
}
