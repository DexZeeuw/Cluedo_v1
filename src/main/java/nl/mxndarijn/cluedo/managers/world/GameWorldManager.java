package nl.mxndarijn.cluedo.managers.world;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.data.SpecialDirectories;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GameInfo;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class GameWorldManager {
    private static GameWorldManager instance;
    private final ArrayList<Game> games;

    private GameWorldManager() {
        this.games = new ArrayList<>();
        deleteGameWorlds();

    }

    public static GameWorldManager getInstance() {
        if (instance == null) {
            instance = new GameWorldManager();
        }
        return instance;
    }

    private void deleteGameWorlds() {
        Logger.logMessage(LogLevel.INFORMATION, Prefix.WORLD_MANAGER, "Deleting old game worlds...");
        try {
            FileUtils.deleteDirectory(SpecialDirectories.GAMES_WORLDS.getDirectory());
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.WORLD_MANAGER, "Could not delete current game worlds");
            e.printStackTrace();
        }
        SpecialDirectories.GAMES_WORLDS.getDirectory().mkdirs();
    }

    public void addGame(Game game) {
        this.games.add(game);
    }

    public void removeGame(Game game) {
        this.games.remove(game);
    }

    public Optional<Game> getGameByWorldUID(UUID uid) {
        for (Game game : games) {
            if (game.getMxWorld().isPresent()) {
                if (game.getMxWorld().get().getWorldUID().equals(uid)) {
                    return Optional.of(game);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Game> getGameByGameInfo(GameInfo upcomingGame) {
        for (Game game : games) {
            if (game.getGameInfo() == upcomingGame) {
                return Optional.of(game);
            }
        }
        return Optional.empty();
    }

    public boolean isPlayerInAGame(UUID uniqueId) {
        for (Game game : games) {
            if (game.getHosts().contains(uniqueId) || game.getGamePlayerOfPlayer(uniqueId).isPresent())
                return true;
        }
        return false;
    }

    public boolean isPlayerPLayingInAGame(UUID uniqueId) {
        for (Game game : games) {
            if (game.getGamePlayerOfPlayer(uniqueId).isPresent())
                return true;
        }
        return false;
    }
}
