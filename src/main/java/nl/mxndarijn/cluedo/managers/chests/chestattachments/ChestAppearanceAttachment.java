package nl.mxndarijn.cluedo.managers.chests.chestattachments;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.ChestAppearance;
import nl.mxndarijn.cluedo.managers.chests.ChestInformation;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestAppearanceAttachment extends ChestAttachment {
    private ChestAppearance appearance;

    public static Optional<ChestAppearanceAttachment> createFromSection(Map<String, Object> section, ChestInformation inf) {
        ChestAppearanceAttachment attachment = new ChestAppearanceAttachment();
        if (!getDefaultValues(attachment, inf, section)) {
            return Optional.empty();
        }
        assert (section != null);

        if (!section.containsKey("appearance")) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load appearance.  Type: " + attachment.type);
            return Optional.empty();
        }
        if (!attachment.setAppearance(ChestAppearance.valueOf((String) section.get("appearance")))) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not find appearance.  Type: " + attachment.type);
            return Optional.empty();
        }

        return Optional.of(attachment);
    }

    public static ChestAttachment createNewInstance(String type, ChestInformation inf) {
        ChestAppearanceAttachment attachment = new ChestAppearanceAttachment();
        attachment.setDefaults(type, inf);
        attachment.appearance = ChestAppearance.CHOICE_THREE;
        return attachment;
    }

    public boolean setAppearance(ChestAppearance appearance) {
        this.appearance = appearance;

        return appearance != null;
    }

    @Override
    public Map<String, Object> getDataForSaving() {
        Map<String, Object> map = new HashMap<>();
        getDataDefaults(map);
        map.put("appearance", appearance.name());

        return map;
    }

    @Override
    public Pair<ItemStack, MxItemClicked> getEditAttachmentItem() {
        return new Pair<>(
                MxSkullItemStackBuilder.create(1)
                        .setSkinFromHeadsData("rainbow-chest")
                        .setName(ChatColor.GREEN + "Kist uiterlijk")
                        .addBlankLore()
                        .addLore(ChatColor.GRAY + "Uiterlijk: " + appearance.getName())
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment aan te passen.")
                        .build(),
                (mxInv, e) -> {
                    Player p = (Player) e.getWhoClicked();
                    MxInventoryManager.getInstance().addAndOpenInventory(p.getUniqueId(),
                            MxDefaultMenuBuilder.create("Kist uiterlijk", MxInventorySlots.THREE_ROWS)
                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("rainbow-chest")
                                                    .setName(ChatColor.GRAY + "Verander kist uiterlijk")
                                                    .addBlankLore()
                                                    .addLore(ChatColor.GRAY + "Huidige status: " + appearance.getName())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om het kist uiterlijk aan te passen")
                                                    .build(),
                                            13,
                                            (mxInv1, e1) -> {
                                                ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
                                                for (ChestAppearance value : ChestAppearance.values()) {
                                                    list.add(new Pair<>(
                                                            MxSkullItemStackBuilder.create(1)
                                                                    .setSkinFromHeadsData("rainbow-chest")
                                                                    .addBlankLore()
                                                                    .setName(ChatColor.GRAY + value.getName())
                                                                    .addLore(ChatColor.YELLOW + "Klik hier om het uiterlijk aan te passen naar: " + value.getName())
                                                                    .build(),
                                                            (mxInv2, e2) -> {
                                                                appearance = value;
                                                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_ATTACHMENT_APPEARANCE_CHANGED_TO, Collections.singletonList(value.getName())));
                                                                information.openAttachmentsInventory(p);
                                                            }
                                                    ));
                                                }


                                                MxInventoryManager.getInstance().addAndOpenInventory(p.getUniqueId(),
                                                        MxListInventoryBuilder.create("Verander kist uiterlijk", MxInventorySlots.THREE_ROWS)
                                                                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                                                                .setListItems(list)
                                                                .setPrevious(mxInv1)
                                                                .setPreviousItemStackSlot(22)
                                                                .build());
                                            }
                                    )

                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("red-minus")
                                                    .setName(ChatColor.RED + "Verwijder chest attachment")
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om de chest attachment te verwijderen")

                                                    .build(), 18,
                                            (mxInv12, e12) -> {
                                                information.removeChestAttachment(p, this, ChestAttachments.CHEST_COLOR_BIND);
                                                p.closeInventory();
                                                Block b = p.getWorld().getBlockAt(information.getLocation().getLocation(p.getWorld()));
                                                if (!(b.getState() instanceof Chest c))
                                                    return;
                                                c.customName(null);
                                                c.update();
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
    public void onOpenChest(InventoryOpenEvent e) {
        Block b = e.getInventory().getLocation().getWorld().getBlockAt(information.getLocation().getLocation(e.getInventory().getLocation().getWorld()));
        Chest chestState = (Chest) b.getState();
        if (chestState.customName() != null && Functions.convertComponentToString(chestState.customName()).equalsIgnoreCase(appearance.getUnicode())) {
            return;
        }
        chestState.customName(Component.text(appearance.getUnicode()));
        chestState.update();

        e.setCancelled(true);
        Player p = (Player) e.getPlayer();
        p.closeInventory();
        p.openInventory(chestState.getInventory());
    }
}
