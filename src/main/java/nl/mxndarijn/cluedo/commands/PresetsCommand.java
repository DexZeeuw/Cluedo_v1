package nl.mxndarijn.cluedo.commands;

import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxcommand.MxCommand;
import nl.mxndarijn.api.mxworld.MxWorld;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.Permissions;
import nl.mxndarijn.cluedo.managers.PresetsManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.presets.Preset;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class PresetsCommand extends MxCommand {


    public PresetsCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame, MxWorldFilter worldFilter) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame, worldFilter);
    }

    public PresetsCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame);
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        ArrayList<Preset> presets = PresetsManager.getInstance().getAllPresets();
        MxItemClicked clickedOnNonConfiguredPreset = (mxInv, e1) -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(Cluedo.class), () -> {
                if (e1.getCurrentItem() != null) {
                    ItemStack is = e1.getCurrentItem();
                    ItemMeta im = is.getItemMeta();
                    PersistentDataContainer container = im.getPersistentDataContainer();
                    Optional<Preset> optionalPreset = PresetsManager.getInstance().getPresetById(container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), Preset.PRESET_ITEMMETA_TAG), PersistentDataType.STRING));
                    if (optionalPreset.isPresent()) {
                        Preset preset = optionalPreset.get();
                        p.closeInventory();
                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_PRESETS_LOADING_WORLD, Collections.emptyList()));
                        preset.loadWorld().thenAccept(loaded -> {
                            if (loaded) {
                                MxWorld mxWorld = preset.getMxWorld().get();
                                World w = Bukkit.getWorld(mxWorld.getWorldUID());
                                if (w != null) {
                                    p.teleport(w.getSpawnLocation());
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_PRESETS_NOW_IN_PRESET, Collections.emptyList()));
                                } else {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_PRESETS_WORLD_NOT_FOUND_BUT_LOADED, Collections.emptyList()));
                                }
                            }
                        });
                    } else {
                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_PRESETS_WORLD_COULD_NOT_BE_LOADED, Collections.emptyList()));
                    }
                }
            });
        };
        ArrayList<Pair<ItemStack, MxItemClicked>> list = presets.stream().map(preset -> new Pair<>(preset.getItemStack(), clickedOnNonConfiguredPreset)).collect(Collectors.toCollection(ArrayList::new));
        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Configureer presets", MxInventorySlots.SIX_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                .setPreviousItemStackSlot(46)
                .setListItems(list)
                .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                        .setName(ChatColor.GRAY + "Info")
                        .addLore(" ")
                        .addLore(ChatColor.YELLOW + "Klik op een preset om deze te configuren.")
                        .build(), 49, null).build());
    }
}
