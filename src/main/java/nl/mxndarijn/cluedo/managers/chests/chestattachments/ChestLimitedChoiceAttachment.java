package nl.mxndarijn.cluedo.managers.chests.chestattachments;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.chests.ChestInformation;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChestLimitedChoiceAttachment extends ChestAttachment {
    private static final HashMap<Integer, String> map = new HashMap<>() {{
        put(0, "number-zero");
        put(1, "number-one");
        put(2, "number-two");
        put(3, "number-three");
        put(4, "number-four");
        put(5, "number-five");
        put(6, "number-six");
        put(7, "number-seven");
        put(8, "number-eight");
        put(9, "number-nine");
    }};
    private static final String moreThan9 = "wooden-plus";
    private int choices;
    private String currentHead = "";

    public static Optional<ChestAttachment> createFromSection(Map<String, Object> section, ChestInformation inf) {
        ChestLimitedChoiceAttachment attachment = new ChestLimitedChoiceAttachment();
        if (!getDefaultValues(attachment, inf, section)) {
            return Optional.empty();
        }
        assert (section != null);

        if (!section.containsKey("choices")) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load choices amount. Type: " + attachment.type);
            return Optional.empty();
        }
        attachment.setChoices((int) section.get("choices"));

        return Optional.of(attachment);
    }

    public static ChestAttachment createNewInstance(String type, ChestInformation inf) {
        ChestLimitedChoiceAttachment attachment = new ChestLimitedChoiceAttachment();
        attachment.setDefaults(type, inf);
        attachment.choices = 1;
        return attachment;
    }

    public void setChoices(int choices) {
        this.choices = choices;
    }

    @Override
    public Map<String, Object> getDataForSaving() {
        Map<String, Object> map = new HashMap<>();
        getDataDefaults(map);
        map.put("choices", choices);

        return map;
    }

    @Override
    public Pair<ItemStack, MxItemClicked> getEditAttachmentItem() {
        return new Pair<>(
                MxSkullItemStackBuilder.create(1)
                        .setSkinFromHeadsData("open-chest")
                        .setName(ChatColor.GREEN + "Keuze kist")
                        .addBlankLore()
                        .addLore(ChatColor.GRAY + "Max items te verwijderen: " + choices)
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment aan te passen.")
                        .build(),
                (mxInv, e) -> {
                    Player p = (Player) e.getWhoClicked();
//                    Optional<nl.mxndarijn.world.map.Map> opt = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());
//                    if(opt.isEmpty())
//                        return;
//
//                    nl.mxndarijn.world.map.Map map = opt.get();
                    MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Keuze kist", MxInventorySlots.THREE_ROWS)
                            .setItem(MxSkullItemStackBuilder.create(1)
                                            .setSkinFromHeadsData("open-chest")
                                            .setName(ChatColor.GREEN + "Verander aantal")
                                            .addBlankLore()
                                            .addLore(ChatColor.GRAY + "Max items te verwijderen: " + choices)
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik hier om het aantal items dat uit de kist kan worden gehaald")
                                            .addLore(ChatColor.YELLOW + "te veranderen.")
                                            .build(),
                                    13,
                                    (mxInv1, e1) -> {
                                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_LIMITED_ENTER_NEW_AMOUNT));
                                        p.closeInventory();
                                        MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                            try {
                                                int i = Integer.parseInt(message);
                                                choices = i;
                                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_LIMITED_CHANGED_TO, Collections.singletonList(i + "")));

                                            } catch (NumberFormatException ee) {
                                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_LIMITED_NOT_A_NUMBER));
                                            }
                                        });
                                    }

                            )
                            .setItem(MxSkullItemStackBuilder.create(1)
                                            .setSkinFromHeadsData("red-minus")
                                            .setName(ChatColor.RED + "Verwijder chest attachment")
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik hier om de chest attachment te verwijderen")

                                            .build(), 18,
                                    (mxInv12, e12) -> {
                                        information.removeChestAttachment(p, this, ChestAttachments.CHEST_LIMITED_CHOICE);
                                        p.closeInventory();
                                    }
                            )
                            .setItem(MxDefaultItemStackBuilder.create(Material.BARRIER)
                                            .setName(ChatColor.GRAY + "Terug")
                                            .build()
                                    , 22,
                                    (mxInv13, e13) -> {
                                        information.openAttachmentsInventory(p);
                                    })
                            .build()

                    );
                }

        );
    }

    @Override
    public void onGameStart(Game game) {
        super.onGameStart(game);
        spawnArmorStand();
    }

    @Override
    public void onGameUpdate(long delta) {
        super.onGameUpdate(delta);
        if (armorStand.isEmpty())
            return;
        String head = map.getOrDefault(choices, moreThan9);
        if (!currentHead.equals(head)) {
            armorStand.get().getEquipment().setHelmet(MxSkullItemStackBuilder.create(1).setSkinFromHeadsData(head).build());
            currentHead = head;
        }

    }

    @Override
    public void onChestInventoryClick(GamePlayer gamePlayer, InventoryClickEvent e, Game game, Player p) {
        if (choices == 0) {
            e.setCancelled(true);
            return;
        }
        Inventory inv = e.getClickedInventory();
        InventoryAction a = e.getAction();
        if (a == InventoryAction.COLLECT_TO_CURSOR || a == InventoryAction.PICKUP_ALL) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;
            choices--;
        } else {
            e.setCancelled(true);
        }
    }
}
