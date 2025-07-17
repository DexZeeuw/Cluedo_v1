package nl.mxndarijn.cluedo.game;

import org.bukkit.ChatColor;

public enum UpcomingGameStatus {
    WAITING(ChatColor.YELLOW + "Wachtend", true),
    CHOOSING_PLAYERS(ChatColor.YELLOW + "Spelers kiezen", true),
    PLAYING(ChatColor.GREEN + "Bezig", false),
    FREEZE(ChatColor.AQUA + "Freezed", false),
    FINISHED(ChatColor.RED + "Afgelopen", false);

    private final String status;
    private final boolean canJoinQueue;

    UpcomingGameStatus(String status, boolean canJoinQueue) {
        this.status = status;
        this.canJoinQueue = canJoinQueue;
    }

    @Override
    public String toString() {
        return status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isCanJoinQueue() {
        return canJoinQueue;
    }
}
