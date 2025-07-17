package nl.mxndarijn.cluedo.data;

import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum BookFailureAction {

    Kill("kill", 1,
            MxSkullItemStackBuilder.create(1)
                .setName(ChatColor.GRAY + "Kill")
                .setSkinFromHeadsData("ghost")
                .addLore(ChatColor.GRAY + "Met deze actie gaat er een persoon")
                .addLore(ChatColor.GRAY + "dood als het boek niet succesvol is.")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik om deze actie te selecteren")
                .build(),
        List.of(ChatColor.GRAY + "Persoon die dood gaat:"),
        (data, holder) -> {
            AvailablePerson personToKill = data.get(0);
            Player p = holder.getPlayer(personToKill);
            p.setHealth(0);
     }, persons -> {
        return  persons.get(0).getName() + " gaat dood.";
     }),
    CLEARINV("clear-inv", 1,
            MxSkullItemStackBuilder.create(1)
                    .setName(ChatColor.GRAY + "Clear-Inv")
                    .setSkinFromHeadsData("barrier")
                    .addLore(ChatColor.GRAY + "Met deze actie wordt de inventory")
                    .addLore(ChatColor.GRAY + "van de geselecteerde persoon gecleared.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik om deze actie te selecteren")
                    .build(),
            List.of(ChatColor.GRAY + "Persoon die gecleared word:"),
            (data, holder) -> {
                AvailablePerson personToKill = data.get(0);
                Player p = holder.getPlayer(personToKill);
                p.getInventory().clear();
            }, persons -> {
        return  persons.get(0).getName() + " zijn inventory wordt gecleared.";
    }),
    TELEPORT("teleport", 2,
            MxSkullItemStackBuilder.create(1)
                    .setName(ChatColor.GRAY + "Teleport")
                    .setSkinFromHeadsData("ender-pearl")
                    .addLore(ChatColor.GRAY + "Met deze actie wordt iemand")
                    .addLore(ChatColor.GRAY + "naar iemand anders geteleport.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik om deze actie te selecteren")
                    .build(),
            List.of(ChatColor.GRAY + "Persoon die geteleport wordt:", ChatColor.GRAY + "Persoon wordt geteleport naar:"),
            (data, holder) -> {
                AvailablePerson personToTeleport = data.get(0);
                AvailablePerson personToTeleportTo = data.get(1);
                Player p = holder.getPlayer(personToTeleport);
                Player pReceiv = holder.getPlayer(personToTeleportTo);
                p.teleport(pReceiv);
            }, persons -> {
        return  persons.get(0).getName() + " wordt geteleport naar " + persons.get(1).getName();
    }),
    SWITCH("switch", 2,
            MxSkullItemStackBuilder.create(1)
                    .setName(ChatColor.GRAY + "Switch")
                    .setSkinFromHeadsData("ender-pearl")
                    .addLore(ChatColor.GRAY + "Met deze actie worden 2 mensen")
                    .addLore(ChatColor.GRAY + "van locatie verwisseld.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik om deze actie te selecteren")
                    .build(),
            List.of(ChatColor.GRAY + "Persoon 1 die geswitched wordt:", ChatColor.GRAY + "Persoon 2 die geswitched wordt:"),
            (data, holder) -> {
                AvailablePerson personToTeleport = data.get(0);
                AvailablePerson personToTeleportTo = data.get(1);
                Player p = holder.getPlayer(personToTeleport);
                Player pReceiv = holder.getPlayer(personToTeleportTo);
                Location loc = p.getLocation().clone();
                Location loc1 = pReceiv.getLocation().clone();

                p.teleport(loc1);
                pReceiv.teleport(loc);
            }, persons -> {
        return  persons.get(0).getName() + " wordt geswitched met " + persons.get(1).getName();
    });


    private final String type;
    private final int playersNeeded;
    private final BookFailureActionInterface action;
    private final List<String> selectors;
    private final ItemStack is;
    private final BookFailureActionTextInterface textInterface;
    BookFailureAction(String type, int playersNeeded, ItemStack is, List<String> selectors, BookFailureActionInterface action, BookFailureActionTextInterface supplier) {
        this.type = type;
        this.playersNeeded = playersNeeded;
        this.action = action;
        this.selectors = selectors;
        this.is = is;
        this.textInterface = supplier;
    }

    public static void executeBookFailure(String data, BookFailurePlayersHolder holder) {
        String[] split = data.split("\\[");
        Optional<BookFailureAction> optionalAction = getActionByType(split[0]);

        if(optionalAction.isEmpty())
            return;

        BookFailureAction bookFailureAction = optionalAction.get();

        String[] persons = split[1].replaceAll("]", "").split(",");
        List<AvailablePerson> availablePeople = new ArrayList<>();
        for (String person : persons) {
            Optional<AvailablePerson> ap = AvailablePerson.getByType(person);
            if(ap.isEmpty()) {
                Logger.logMessage(LogLevel.ERROR, "Could not execute BookFailureAction, could not find type: " + person);
                return;
            }
            availablePeople.add(ap.get());
        }
        try {
            bookFailureAction.action.execute(availablePeople, holder);
        } catch (Exception e) {
            Logger.logMessage(LogLevel.ERROR, "Er ging iets fout tijdens het uitvoeren van een BookFailureAction action: " + bookFailureAction.toString());
            e.printStackTrace();
        }

    }

    static Optional<BookFailureAction> getActionByType(String type) {
        for (BookFailureAction value1 : values()) {
            if(value1.type.equalsIgnoreCase(type))
                return Optional.of(value1);
        }
        return Optional.empty();
    }

    public interface BookFailureActionInterface {
        void execute(List<AvailablePerson> persons, BookFailurePlayersHolder holder);
    }

    public interface BookFailureActionTextInterface {
        String getText(List<AvailablePerson> persons);
    }

    public String getType() {
        return type;
    }

    public int getPlayersNeeded() {
        return playersNeeded;
    }

    public BookFailureActionInterface getAction() {
        return action;
    }

    public List<String> getSelectors() {
        return selectors;
    }

    public ItemStack getIs() {
        return is;
    }

    public BookFailureActionTextInterface getTextInterface() {
        return textInterface;
    }
}

