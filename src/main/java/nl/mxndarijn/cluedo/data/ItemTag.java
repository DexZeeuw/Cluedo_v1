package nl.mxndarijn.cluedo.data;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.inventory.*;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.cluedo.Cluedo;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.managers.MapManager;
import nl.mxndarijn.cluedo.managers.language.LanguageManager;
import nl.mxndarijn.cluedo.managers.language.LanguageText;
import nl.mxndarijn.cluedo.managers.world.GameWorldManager;
import nl.mxndarijn.cluedo.map.Map;
import nl.mxndarijn.cluedo.map.mapplayer.MapPlayer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public enum ItemTag {
    NOTSILENT("notsilent", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("redstone-torch")
                .setName(ChatColor.GRAY + "Silent")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Not silent" : ChatColor.RED + "silent"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "notsilent";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Silent";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    }),
    DROPPABLE("droppable", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("dropper")
                .setName(ChatColor.GRAY + "Droppable")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Droppable" : ChatColor.RED + "Undroppable"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "droppable";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Undroppable";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    }),
    VANISHABLE("vanishable", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("ghost")
                .setName(ChatColor.GRAY + "Vanish")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Blijft" : ChatColor.RED + "Verdwijnt"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "vanishable";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Vanish";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    }),
    ITEM_LOCK("itemlock", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("redstone-block")
                .setName(ChatColor.GRAY + "Item-Lock")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Verplaatsbaar" : ChatColor.RED + "Locked"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "itemlock";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Item-Lock";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));

        if (!dataBoolean) {
            e.setCancelled(true);
        }
    }),
    PLACEABLE("placeable", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("piston")
                .setName(ChatColor.GRAY + "Placeable")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Placeable" : ChatColor.RED + "Unplaceable"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "placeable";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Unplaceable";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);
        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    }),
    COLORBIND("colorbind", data -> {
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("blue-block")
                .setName(ChatColor.GRAY + "Colorbind")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de colorbind van dit item aan te passen")
                .build();
    }, (mxInv, e) -> {
        AtomicReference<List<Colors>> colors = new AtomicReference<>(new ArrayList<>());
        Optional<Map> optionalMap = MapManager.getInstance().getMapByWorldUID(e.getWhoClicked().getWorld().getUID());
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(e.getWhoClicked().getWorld().getUID());
        optionalMap.ifPresent(map -> {
            colors.set(map.getMapConfig().getColors().stream().map(MapPlayer::getColor).collect(Collectors.toList()));
        });
        optionalGame.ifPresent(game -> {
            colors.set(game.getColors().stream().map(g -> g.getMapPlayer().getColor()).collect(Collectors.toList()));
        });

        List<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        List<Colors> addedColors = new ArrayList<>();
        String key = "colorbind";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String specialTag = ChatColor.RED + "" + ChatColor.GRAY + ChatColor.GRAY + ChatColor.DARK_AQUA + ChatColor.UNDERLINE + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.RESET;
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);
        if (data != null) {
            for (String s : data.split(";")) {
                if (Colors.getColorByType(s).isPresent())
                    addedColors.add(Colors.getColorByType(s).get());
            }
        }

        colors.get().forEach(color -> {
            list.add(new Pair<>(MxSkullItemStackBuilder.create(1)
                    .setName(ChatColor.GRAY + color.getDisplayNameWithoutColor())
                    .setSkinFromHeadsData(color.getHeadKey())
                    .addBlankLore()
                    .addLore(ChatColor.GRAY + "Status: " + (addedColors.contains(color) ? ChatColor.GREEN + "Toegevoegd" : ChatColor.RED + "Niet toegevoegd"))
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik hier om deze kleur te togglen.")
                    .build(), (mxInv1, e1) -> {
                if (addedColors.contains(color)) {
                    addedColors.remove(color);
                    e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.COLORBIND_REMOVED));
                } else {
                    e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.COLORBIND_ADDED));
                    addedColors.add(color);
                }
                StringBuilder dataTag = new StringBuilder();
                addedColors.forEach(c -> {
                    dataTag.append(c.getType()).append(";");
                });
                if(!addedColors.isEmpty())
                    dataTag.deleteCharAt(dataTag.length() - 1);
                container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataTag.toString());
                List<Component> loreList = im.hasLore() ? im.lore() : new ArrayList<>();
                List<Component> newLoreList = loreList.stream().filter(lore -> !Functions.convertComponentToString(lore).contains(specialTag)).collect(Collectors.toList());
                if (!addedColors.isEmpty()) {
                    newLoreList.add(Component.text(specialTag + ChatColor.BLUE + "Colorbind:"));
                    addedColors.forEach(c -> {
                        newLoreList.add(Component.text(specialTag + ChatColor.GRAY + " - " + c.getDisplayName()));
                    });
                }
                im.lore(newLoreList);
                is.setItemMeta(im);

                e1.setCurrentItem(MxSkullItemStackBuilder.create(1)
                        .setName(ChatColor.GRAY + color.getDisplayNameWithoutColor())
                        .setSkinFromHeadsData(color.getHeadKey())
                        .addBlankLore()
                        .addLore(ChatColor.GRAY + "Status: " + (addedColors.contains(color) ? ChatColor.GREEN + "Toegevoegd" : ChatColor.RED + "Niet toegevoegd"))
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik hier om deze kleur te togglen.")
                        .build());
            }));
        });


        MxInventoryManager.getInstance().addAndOpenInventory((Player) e.getWhoClicked(), MxListInventoryBuilder.create(ChatColor.GRAY + "Colorbind", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setPrevious(mxInv)
                .setShowPageNumbers(false)
                .setListItems(list)
                .build());
    }),
    CLEARABLE("clearable", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("barrier")
                .setName(ChatColor.GRAY + "Clearable")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Clearable" : ChatColor.RED + "Unclearable"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "clearable";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Unclearable";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);

        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    }),
    DETECTABLE("detectable", data -> {
        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("sculk-sensor")
                .setName(ChatColor.GRAY + "Detectable")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (dataBoolean ? ChatColor.GREEN + "Detectable" : ChatColor.RED + "Undetectable"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen.")
                .build();
    }, (mxInv, e) -> {
        String key = "detectable";
        ItemStack is = e.getWhoClicked().getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        PersistentDataContainer container = im.getPersistentDataContainer();
        String data = container.get(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING);

        boolean dataBoolean = true;
        if (data != null && data.equalsIgnoreCase("false"))
            dataBoolean = false;
        dataBoolean = !dataBoolean;
        container.set(new NamespacedKey(JavaPlugin.getPlugin(Cluedo.class), key), PersistentDataType.STRING, dataBoolean + "");
        String lore = ChatColor.RED + "Undetectable";
        List<Component> list = im.hasLore() ? im.lore() : new ArrayList<>();
        if (dataBoolean) {
            List<Component> newList = new ArrayList<>();
            list.forEach(c -> {
                if (!Functions.convertComponentToString(c).equalsIgnoreCase(lore)) {
                    newList.add(c);
                }
            });
            list = newList;
        } else {
            list.add(Component.text(lore));
        }
        im.lore(list);
        is.setItemMeta(im);

        e.getWhoClicked().closeInventory();
        e.getWhoClicked().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.ITEMTAG_CHANGED));
    });


    private final String persistentDataTag;
    private final ItemTagContainer container;
    private final MxItemClicked clicked;

    ItemTag(String persistentDataTag, ItemTagContainer item, MxItemClicked clicked) {
        this.persistentDataTag = persistentDataTag;
        this.container = item;
        this.clicked = clicked;

    }

    public String getPersistentDataTag() {
        return persistentDataTag;
    }

    public ItemTagContainer getContainer() {
        return container;
    }

    public MxItemClicked getClicked() {
        return clicked;
    }
}

