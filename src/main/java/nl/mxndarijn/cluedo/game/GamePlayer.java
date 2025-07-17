package nl.mxndarijn.cluedo.game;

import nl.mxndarijn.api.mxscoreboard.MxSupplierScoreBoard;
import nl.mxndarijn.cluedo.data.PeacekeeperLoot;
import nl.mxndarijn.cluedo.data.ScoreBoard;
import nl.mxndarijn.cluedo.managers.ScoreBoardManager;
import nl.mxndarijn.cluedo.map.mapplayer.MapPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class GamePlayer {

    private final boolean canReborn;
    private MapPlayer mapPlayer;
    private Optional<UUID> player;
    private boolean alive;
    private MxSupplierScoreBoard scoreboard;
    private boolean beginChestOpened;
    private boolean peacekeeperChestOpened;
    private Game game;
    private JavaPlugin plugin;
    private Optional<GamePlayer> votedOn;

    public GamePlayer(MapPlayer mapPlayer, JavaPlugin plugin, Game game) {
        this.mapPlayer = mapPlayer;
        this.player = Optional.empty();
        this.game = game;
        this.plugin = plugin;
        alive = true;
        this.beginChestOpened = false;
        this.peacekeeperChestOpened = false;
        this.votedOn = Optional.empty();
        this.canReborn = !mapPlayer.isPeacekeeper();
        String host = Bukkit.getOfflinePlayer(game.getMainHost()).getName();
        scoreboard = new MxSupplierScoreBoard(plugin, () -> {
            if(player.isPresent()) {
                Player p = Bukkit.getPlayer(player.get());
                if (p != null) {
                    return PlaceholderAPI.setPlaceholders(p, ScoreBoard.GAME_HOST.getTitle(new HashMap<>() {{
                        put("%%map_name%%", game.getConfig().getPresetConfig().getName());
                    }}));
                }
            }
            return ScoreBoard.GAME_HOST.getTitle(new HashMap<>() {{
                put("%%map_name%%", game.getConfig().getPresetConfig().getName());
            }});
        }, () -> {
            if(player.isPresent()) {
                Player p = Bukkit.getPlayer(player.get());
                if (p != null) {
                    return PlaceholderAPI.setPlaceholders(p, ScoreBoard.GAME_PLAYER.getLines(new HashMap<>() {{
                        put("%%game_status%%", game.getGameInfo().getStatus().getStatus());
                        put("%%game_time%%", game.getGameTime());
                        put("%%color%%", mapPlayer.getColor().getDisplayName());
                        put("%%host%%", host);
                        put("%%role%%", (beginChestOpened ? (peacekeeperChestOpened ? mapPlayer.getRoleDisplayString() : mapPlayer.getRoleDisplayWithoutPeacekeeper()) : ChatColor.GRAY + "Onbekend"));
                    }}));
                }
            }
            return ScoreBoard.GAME_PLAYER.getLines(new HashMap<>() {{
                put("%%game_status%%", game.getGameInfo().getStatus().getStatus());
                put("%%game_time%%", game.getGameTime());
                put("%%color%%", mapPlayer.getColor().getDisplayName());
                put("%%host%%", host);
                put("%%role%%", (beginChestOpened ? (peacekeeperChestOpened ? mapPlayer.getRoleDisplayString() : mapPlayer.getRoleDisplayWithoutPeacekeeper()) : ChatColor.GRAY + "Onbekend"));
            }});
        });
        scoreboard.setUpdateTimer(10);
    }

    public void setPlayingPlayer(UUID player) {
        this.player.ifPresent(p -> {
            scoreboard.removePlayer(p);
        });
        if (player != null) {
            this.player = Optional.of(player);
            ScoreBoardManager.getInstance().setPlayerScoreboard(player, scoreboard);
        } else {
            this.player = Optional.empty();
        }
    }

    public MapPlayer getMapPlayer() {
        return mapPlayer;
    }

    public void setMapPlayer(MapPlayer mapPlayer) {
        this.mapPlayer = mapPlayer;
    }

    public Optional<UUID> getPlayer() {
        return player;
    }

    public void setPlayer(Optional<UUID> player) {
        this.player = player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public MxSupplierScoreBoard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(MxSupplierScoreBoard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public boolean isBeginChestOpened() {
        return beginChestOpened;
    }

    public void setBeginChestOpened(boolean beginChestOpened) {
        this.beginChestOpened = beginChestOpened;
    }

    public boolean isPeacekeeperChestOpened() {
        return peacekeeperChestOpened;
    }

    public void setPeacekeeperChestOpened(boolean peacekeeperChestOpened) {
        this.peacekeeperChestOpened = peacekeeperChestOpened;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void givePeacekeeperLoot() {
        if (player.isPresent()) {
            Player p = Bukkit.getPlayer(player.get());
            if (p != null) {
                for (PeacekeeperLoot loot : PeacekeeperLoot.values()) {
                    if (loot.getSlot() == null) {
                        p.getInventory().addItem(loot.getIs());
                    } else {
                        p.getInventory().setItem(loot.getSlot(), loot.getIs());
                    }
                }
            }
        }
    }

    public Optional<GamePlayer> getVotedOn() {
        return votedOn;
    }

    public void setVotedOn(Optional<GamePlayer> votedOn) {
        this.votedOn = votedOn;
    }
}
