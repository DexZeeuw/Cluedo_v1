package nl.mxndarijn.cluedo.game.events;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.GameManager;
import nl.mxndarijn.cluedo.managers.ScoreBoardManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Optional;

public class GameDefaultEvents extends GameEvent {

    public GameDefaultEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler
    public void paintingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() == null)
            return;
        if (!validateWorld(e.getRemover().getWorld()))
            return;
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getRemover().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        if (e.getEntity() instanceof Painting) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty() && !game.getHosts().contains(e.getPlayer().getUniqueId()))
            return;
        e.joinMessage(Component.text(""));
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_JOINED_AGAIN, Collections.singletonList(e.getPlayer().getName())));
        gamePlayer.ifPresent(player -> ScoreBoardManager.getInstance().setPlayerScoreboard(e.getPlayer().getUniqueId(), player.getScoreboard()));
        if (game.getHosts().contains(e.getPlayer().getUniqueId())) {
            ScoreBoardManager.getInstance().setPlayerScoreboard(e.getPlayer().getUniqueId(), game.getHostScoreboard());
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        GameManager.getInstance().getUpcomingGameList().forEach(gameInfo -> {
            gameInfo.getQueue().remove(e.getPlayer().getUniqueId());
        });
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty() && !game.getHosts().contains(e.getPlayer().getUniqueId()))
            return;
        e.quitMessage(Component.text(""));
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_LEAVED_AGAIN, Collections.singletonList(e.getPlayer().getName())));
    }

}
