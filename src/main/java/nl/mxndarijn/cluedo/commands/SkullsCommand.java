package nl.mxndarijn.cluedo.commands;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.heads.MxHeadManager;
import nl.mxndarijn.api.inventory.heads.MxHeadSection;
import nl.mxndarijn.api.inventory.heads.MxHeadsType;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxcommand.MxCommand;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.data.ChatPrefix;
import nl.mxndarijn.cluedo.data.Permissions;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SkullsCommand extends MxCommand {

    public SkullsCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame, MxWorldFilter worldFilter) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame, worldFilter);
    }

    public SkullsCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame);
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        MxItemClicked giver = (inv, e) -> {
            if (e.getCurrentItem() != null) {
                ItemStack skullItemStack = e.getCurrentItem().clone();
                ItemMeta im = skullItemStack.getItemMeta();
                im.lore(Collections.emptyList());
                skullItemStack.setItemMeta(im);
                if (p.hasPermission(Permissions.COMMAND_SKULLS_REMOVE_SKULL.getPermission())) {
                    if (e.getClick() == ClickType.SHIFT_LEFT) {
                        MxItemClicked deleteSkull = (mxInv, e12) -> {
                            if (e12.getCurrentItem() != null) {
                                ItemStack is = e12.getCurrentItem();
                                boolean deleteSkullItem = is.getType() == Material.LIME_STAINED_GLASS_PANE;
                                e.getWhoClicked().closeInventory();
                                if (deleteSkullItem) {
                                    PersistentDataContainer container = im.getPersistentDataContainer();
                                    MxHeadManager.getInstance().removeHead(container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), "skull_key"), PersistentDataType.STRING));
                                    e.getWhoClicked().sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_DELETED, Collections.emptyList()));
                                } else {
                                    MxInventoryManager.getInstance().addAndOpenInventory(p, inv);
                                    e.getWhoClicked().sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_NOT_DELETED, Collections.emptyList()));
                                }

                            }
                        };
                        MxDefaultMenuBuilder menuBuilder =
                                MxDefaultMenuBuilder.create(ChatColor.GRAY + "Skull verwijderen", MxInventorySlots.THREE_ROWS)
                                        .setItem(skullItemStack, 13, null)
                                        .setItem(MxDefaultItemStackBuilder.create(Material.LIME_STAINED_GLASS_PANE, 1)
                                                .setName(ChatColor.GREEN + "Ja")
                                                .addLore(" ")
                                                .addLore(ChatColor.YELLOW + "Ja, verwijder de skull.")
                                                .build(), 15, deleteSkull)
                                        .setItem(MxDefaultItemStackBuilder.create(Material.RED_STAINED_GLASS_PANE, 1)
                                                .setName(ChatColor.RED + "Nee")
                                                .addLore(" ")
                                                .addLore(ChatColor.YELLOW + "Nee, verwijder de skull niet.")
                                                .build(), 11, deleteSkull)
                                        .setPrevious(inv);

                        MxInventoryManager.getInstance().addAndOpenInventory(p, menuBuilder.build());
                        return;
                    }
                }
                e.getWhoClicked().getInventory().addItem(skullItemStack);
            }
        };
        MxHeadManager instance = MxHeadManager.getInstance();
        instance.getAllHeadKeys().forEach(key -> {
            Optional<MxHeadSection> section = instance.getHeadSection(key);
            section.ifPresent(mxHeadSection -> {
                MxSkullItemStackBuilder b = MxSkullItemStackBuilder.create(1)
                        .setSkinFromHeadsData(key)
                        .setName(ChatColor.GRAY + mxHeadSection.getName().get())
                        .addLore(" ")
                        .addLore(ChatColor.YELLOW + "Klik om dit item toe te voegen aan je inventory.")
                        .addCustomTagString("skull_key", mxHeadSection.getKey());
                if (p.hasPermission(Permissions.COMMAND_SKULLS_REMOVE_SKULL.getPermission())) {
                    b.addLore(ChatColor.YELLOW + "Shift-klik op dit item om het te verwijderen.");
                }
                list.add(new Pair<>(b.build(), giver));
            });
        });
        MxListInventoryBuilder builder =
                MxListInventoryBuilder.create(ChatColor.RED + "Heads-Database", MxInventorySlots.SIX_ROWS)
                        .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                        .addListItems(list)
                        .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                                .setName(ChatColor.GRAY + "Info")
                                .addLore(" ")
                                .addLore(ChatColor.GRAY + "Wil je een skull toevoegen?")
                                .addLore(ChatColor.GRAY + "Vraag een stafflid dit te doen.")
                                .build(), 48, null);

        if (p.hasPermission(Permissions.COMMAND_SKULLS_ADD_SKULL.getPermission())) {
            builder.setItem(MxDefaultItemStackBuilder.create(Material.SKELETON_SKULL)
                    .setName(ChatColor.GRAY + "Voeg een skull toe")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Klik hier om een skull toe te voegen.")
                    .build(), 50, (inv, e) -> {
                MxDefaultMenuBuilder menuBuilder = MxDefaultMenuBuilder.create(ChatColor.GRAY + "Skull toevoegen", MxInventorySlots.FIVE_ROWS)
                        .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                                .setName(ChatColor.GRAY + "Info")
                                .addLore(" ")
                                .addLore(ChatColor.GRAY + "Klik op de skull die je wilt toevoegen.")
                                .addLore(ChatColor.GRAY + "De skull moet in je inventory zitten")
                                .addLore(ChatColor.GRAY + "om hem toe te kunnen voegen.")
                                .build(), 40, null)
                        .setPrevious(inv);

                MxItemClicked clickedOnSkull = (mxInv, e1) -> {
                    if (e1.getCurrentItem() == null)
                        return;
                    ItemStack skull = e1.getCurrentItem();
                    MxItemClicked clickedOnPlayerSkullQuestion = (mxInv1, e2) -> {
                        if (e2.getCurrentItem() != null) {
                            ItemStack is = e2.getCurrentItem();
                            boolean isPlayerSkull = is.getType() == Material.LIME_STAINED_GLASS_PANE;
                            e.getWhoClicked().closeInventory();
                            e.getWhoClicked().sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_GET_CHAT_INPUT, Collections.emptyList()));
                            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                if (MxHeadManager.getInstance().storeSkullTexture(skull, UUID.randomUUID().toString(), message, isPlayerSkull ? MxHeadsType.PLAYER : MxHeadsType.MANUALLY_ADDED)) {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_SKULL_ADDED, Collections.emptyList()));
                                } else {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_SKULL_NOT_ADDED, Collections.emptyList()));
                                }
                            });
                        }
                    };
                    MxDefaultMenuBuilder menuBuilder1 = MxDefaultMenuBuilder.create(ChatColor.GRAY + "Skull toevoegen", MxInventorySlots.THREE_ROWS)
                            .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                                    .setName(ChatColor.GRAY + "Info")
                                    .addLore(" ")
                                    .addLore(ChatColor.GRAY + "Is het item een PlayerSkull:")
                                    .addLore(ChatColor.GRAY + "- Het is geen custom skull.")
                                    .addLore(ChatColor.GRAY + "- Elke restart moet de skin opnieuw worden opgevraagd.")
                                    .addLore(ChatColor.GRAY + "- De skull heeft een eigenaar.")
                                    .build(), 13, null)
                            .setItem(MxDefaultItemStackBuilder.create(Material.LIME_STAINED_GLASS_PANE, 1)
                                    .setName(ChatColor.GREEN + "Ja")
                                    .addLore(" ")
                                    .addLore(ChatColor.YELLOW + "Ja, het is een PlayerSkull.")
                                    .build(), 15, clickedOnPlayerSkullQuestion)
                            .setItem(MxDefaultItemStackBuilder.create(Material.RED_STAINED_GLASS_PANE, 1)
                                    .setName(ChatColor.RED + "Nee")
                                    .addLore(" ")
                                    .addLore(ChatColor.YELLOW + "Nee, het is geen PlayerSkull.")
                                    .build(), 11, clickedOnPlayerSkullQuestion)
                            .setPrevious(mxInv);
                    MxInventoryManager.getInstance().addAndOpenInventory(p, menuBuilder1.build());
                    e.getWhoClicked().sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_IS_SKULL_PLAYER_SKULL, Collections.emptyList()));
                };
                Arrays.stream(e.getWhoClicked().getInventory().getContents()).forEach(itemStack -> {
                    if (itemStack != null && itemStack.getType() == Material.PLAYER_HEAD) {
                        ItemStack is = itemStack.clone();
                        is.setAmount(1);
                        menuBuilder.addItem(is, clickedOnSkull);
                    }
                });
                MxInventoryManager.getInstance().addAndOpenInventory(p, menuBuilder.build());
            });
        }
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_SKULLS_DEFAULT, Collections.emptyList()));
        MxInventoryManager.getInstance().addAndOpenInventory(
                p,
                builder.build()
        );
    }


}
