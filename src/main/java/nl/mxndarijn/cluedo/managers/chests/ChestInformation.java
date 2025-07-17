package nl.mxndarijn.cluedo.managers.chests;

import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.chests.chestattachments.ChestAttachment;
import nl.mxndarijn.cluedo.managers.chests.chestattachments.ChestAttachments;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ChestInformation {
    private String uuid;
    private String name;
    private MxLocation location;

    private File file;
    private String path;

    private List<ChestAttachment> chestAttachmentList;

    public ChestInformation(String name, MxLocation location) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.chestAttachmentList = new ArrayList<>();
    }

    private ChestInformation() {

    }

    public static Optional<ChestInformation> load(ConfigurationSection section) {
        if (section == null) {
            return Optional.empty();
        }
        ChestInformation i = new ChestInformation();
        i.uuid = section.getName();
        i.name = section.getString("name");
        Optional<MxLocation> optionalMxLocation = MxLocation.loadFromConfigurationSection(section.getConfigurationSection("location"));
        optionalMxLocation.ifPresent(location -> i.location = location);
        i.chestAttachmentList = new ArrayList<>(); //TODO Load items
        section.getMapList("attachments").forEach(map -> {
            Map<String, Object> convertedMap = (Map<String, Object>) map;
            String type = (String) convertedMap.get("type");
            Optional<ChestAttachments> attachment = ChestAttachments.getAttachmentByType(type);
            if (attachment.isEmpty()) {
                Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load attachment (Type not found) : " + type);
            } else {
                Optional<ChestAttachment> opt = attachment.get().getExistingInstance(convertedMap, i);
                if (opt.isEmpty()) {
                    Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER + "Could not load attachment " + type);
                }
                opt.ifPresent(att -> {
                    i.chestAttachmentList.add(att);
                });
            }

        });

        if (i.location != null) {
            return Optional.of(i);
        }
        return Optional.empty();
    }

    public void save(FileConfiguration fc) {
        ConfigurationSection section = fc.createSection(uuid);
        section.set("name", name);
        location.write(section.createSection("location"));

        List<Map<String, Object>> list = new ArrayList<>();
        chestAttachmentList.forEach(chestAttachment -> {
            list.add(chestAttachment.getDataForSaving());
        });
        section.set("attachments", list);
    }


    public String getName() {
        return name;
    }

    private boolean containsAttachment(ChestAttachments attachments) {
        for (ChestAttachment at : chestAttachmentList) {
            if (at.getClass().equals(attachments.getAttachmentClass())) {
                return true;
            }
        }
        return false;
    }

    public void openAttachmentsInventory(Player p) {
        p.closeInventory();
        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        Arrays.stream(ChestAttachments.values()).forEach(attachments -> {
            if (!containsAttachment(attachments))
                list.add(attachments.getAddItemStack(this));
        });

        chestAttachmentList.forEach(chestAttachment -> {
            list.add(chestAttachment.getEditAttachmentItem());
        });

        Collections.reverse(list);

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Chest Attachments", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setListItems(list)
                .build());
    }

    public MxLocation getLocation() {
        return location;
    }

    public void addNewAttachment(Player p, ChestAttachments attachments) {
        if (containsAttachment(attachments)) {
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_COULD_NOT_ADD, Collections.singletonList(attachments.getDisplayName())));
            p.closeInventory();
            return;
        }
        Optional<ChestAttachment> attachment = attachments.createNewInstance(this);
        if (attachment.isPresent()) {
            chestAttachmentList.add(attachment.get());
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_ADDED, Collections.singletonList(attachments.getDisplayName())));
            p.closeInventory();
            return;
        }
        p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_COULD_NOT_ADD, Collections.singletonList(attachments.getDisplayName())));

    }


    public String getUuid() {
        return uuid;
    }

    public File getFile() {
        return file;
    }


    public List<ChestAttachment> getChestAttachmentList() {
        return chestAttachmentList;
    }

    public void removeChestAttachment(Player p, ChestAttachment at, ChestAttachments chestAttachments) {
        getChestAttachmentList().remove(at);
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_REMOVED, Collections.singletonList(chestAttachments.getDisplayName())));
    }

    public boolean canOpenChest(GamePlayer gamePlayer) {
        for (ChestAttachment c : chestAttachmentList) {
            if (!c.canOpenChest(gamePlayer)) {
                return false;
            }
        }
        return true;
    }

    public void onChestInteract(GamePlayer gamePlayer, PlayerInteractEvent e, Game game, Player p) {
        for (ChestAttachment c : chestAttachmentList) {
            c.onChestInteract(gamePlayer, e, game, p);
        }
    }

    public void onChestInventoryClick(GamePlayer gamePlayer, InventoryClickEvent e, Game game, Player p) {
        for (ChestAttachment c : chestAttachmentList) {
            c.onChestInventoryClick(gamePlayer, e, game, p);
        }

    }

    public boolean containsChestAttachment(ChestAttachments chestAttachments) {
        for (ChestAttachment chestAttachment : chestAttachmentList) {
            if (chestAttachment.getClass().equals(chestAttachments.getAttachmentClass())) {
                return true;
            }
        }
        return false;
    }
}
