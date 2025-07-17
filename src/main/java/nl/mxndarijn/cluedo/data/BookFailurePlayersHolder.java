package nl.mxndarijn.cluedo.data;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class BookFailurePlayersHolder {
    private HashMap<AvailablePerson, Player> map;
    public BookFailurePlayersHolder() {
        map = new HashMap<>();
    }

    public static BookFailurePlayersHolder create() {
        return new BookFailurePlayersHolder();
    }

    public BookFailurePlayersHolder setData(AvailablePerson person, Player p) {
        map.put(person, p);
        return this;
    }

    public Player getPlayer(AvailablePerson person) {
        return map.get(person);
    }
}
