package nl.mxndarijn.cluedo.items.presets;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.menu.MxDefaultInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.managers.PresetsManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.shulkers.ShulkerInformation;
import nl.mxndarijn.cluedo.managers.shulkers.ShulkerManager;
import nl.mxndarijn.cluedo.presets.Preset;
import nl.mxndarijn.cluedo.presets.PresetConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShulkerConfigureTool extends MxItem {


    public ShulkerConfigureTool(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Preset> optionalPreset = PresetsManager.getInstance().getPresetByWorldUID(e.getPlayer().getWorld().getUID());
        if (!optionalPreset.isPresent()) {
            return;
        }

        Preset preset = optionalPreset.get();
        PresetConfig config = preset.getConfig();
        ShulkerManager manager = preset.getShulkerManager();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof ShulkerBox) {
            e.setCancelled(true);
            MxLocation location = MxLocation.getFromLocation(e.getClickedBlock().getLocation());
            if (manager.containsLocation(location)) {
                Optional<ShulkerInformation> information = manager.getShulkerByLocation(location);
                ShulkerInformation inf = information.get();
                MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultInventoryBuilder.create(ChatColor.GRAY + "Verwijder shulker", MxInventorySlots.THREE_ROWS)
                        .setItem(MxDefaultItemStackBuilder.create(Material.LIME_STAINED_GLASS_PANE)
                                        .setName(ChatColor.GREEN + "Verwijder shulker")
                                        .build(),
                                14,
                                (mxInv, e1) -> {
                                    manager.removeShulker(inf);
                                    p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.SHULKER_CONFIGURE_TOOL_CHEST_REMOVED, ChatPrefix.WIDM));
                                    p.closeInventory();
                                }
                        )
                        .setItem(MxDefaultItemStackBuilder.create(Material.RED_STAINED_GLASS_PANE)
                                        .setName(ChatColor.RED + "Behoud shulker")
                                        .build(),
                                12,
                                (mxInv, e1) -> {
                                    p.closeInventory();
                                }
                        )
                        .setItem(MxSkullItemStackBuilder.create(1)
                                        .setSkinFromHeadsData("shulker-light-blue")
                                        .setName(ChatColor.GRAY + "Toggle beginkist")
                                        .addBlankLore()
                                        .addLore(ChatColor.GRAY + "Beginkist: " + (inf.isStartingRoom() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee"))
                                        .addBlankLore()
                                        .addLore(ChatColor.YELLOW + "Klik hier om de beginkist te togglen")
                                        .build(),
                                13,
                                (mxInv, e12) -> {
                                    inf.setStartingRoom(!inf.isStartingRoom());
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.SHULKER_CONFIGURE_TOOL_TOGGLED_SHULKER, Collections.singletonList(inf.isStartingRoom() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee")));
                                    p.closeInventory();
                                })
                        .build());
                return;
            }
            // Create new
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.SHULKER_CONFIGURE_TOOL_ENTER_NAME, ChatPrefix.WIDM));
            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(),
                    message -> {
                        if (manager.containsLocation(location))
                            return;
                        AtomicBoolean bool = new AtomicBoolean(true);
                        preset.getShulkerManager().getShulkers().forEach(shulker -> {
                            if (shulker.getMaterial() == e.getClickedBlock().getType()) {
                                bool.set(false);
                            }
                        });
                        ShulkerInformation shulkerInformation = new ShulkerInformation(message, location, e.getClickedBlock().getType(), bool.get());
                        manager.addShulker(shulkerInformation);
                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.SHULKER_CONFIGURE_TOOL_CHEST_ADDED, Arrays.asList(message, (bool.get() ? "Ja" : "Nee"))));

                    }
            );
        }

    }
}
