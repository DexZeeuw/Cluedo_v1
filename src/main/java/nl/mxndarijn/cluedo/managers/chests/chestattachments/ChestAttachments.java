package nl.mxndarijn.cluedo.managers.chests.chestattachments;

import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.managers.chests.ChestInformation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public enum ChestAttachments {
    CHEST_APPEARANCE("CHEST_APPEARANCE", ChestAppearanceAttachment.class, "Kist-uiterlijk", "rainbow-chest",
            MxSkullItemStackBuilder.create(1)
                    .setSkinFromHeadsData("rainbow-chest")
                    .setName(ChatColor.RED + "Kist uiterlijk")
                    .addBlankLore()
                    .addLore(ChatColor.GRAY + "Deze Attachment zorgt ervoor dat de kist van binnen")
                    .addLore(ChatColor.GRAY + "een ander uiterlijk heeft.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment toe te voegen.")
                    .build()),
    CHEST_COLOR_BIND("CHEST_COLOR_BIND", ChestColorBindAttachment.class, "Kist-color-bind", "wool-chest",
            MxSkullItemStackBuilder.create(1)
                    .setSkinFromHeadsData("wool-chest")
                    .setName(ChatColor.RED + "Colorbind kist")
                    .addBlankLore()
                    .addLore(ChatColor.GRAY + "Deze Attachment zorgt ervoor dat de kist maar door een")
                    .addLore(ChatColor.GRAY + "aantal kleuren geopend kan worden.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment toe te voegen.")
                    .build()),
    CHEST_LIMITED_CHOICE("CHEST_LIMITED_CHOICE", ChestLimitedChoiceAttachment.class, "Kist-limited-items", "open-chest",
            MxSkullItemStackBuilder.create(1)
                    .setSkinFromHeadsData("open-chest")
                    .setName(ChatColor.RED + "Keuze Kist")
                    .addBlankLore()
                    .addLore(ChatColor.GRAY + "Deze Attachment zorgt ervoor dat er maar een x aantal items")
                    .addLore(ChatColor.GRAY + "uit de kist kan worden gehaald")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment toe te voegen.")
                    .build()),
    CHEST_LOCK("CHEST_LOCK", ChestLockAttachment.class, "Kist-lock", "locked-chest",
            MxSkullItemStackBuilder.create(1)
                    .setSkinFromHeadsData("locked-chest")
                    .setName(ChatColor.RED + "Kist slot")
                    .addBlankLore()
                    .addLore(ChatColor.GRAY + "Deze Attachment zorgt ervoor dat de kist eerst moet worden")
                    .addLore(ChatColor.GRAY + "geopent met een sleutel voordat de kisten open kan.")
                    .addBlankLore()
                    .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment toe te voegen.")
                    .build());


    private final Class<? extends ChestAttachment> attachmentClass;
    private final String name;

    private final String displayName;
    private final ItemStack is;
    private final String skullName;

    ChestAttachments(String name, Class<? extends ChestAttachment> attachment, String displayName, String skullName, ItemStack is) {
        this.attachmentClass = attachment;
        this.name = name;
        this.displayName = displayName;
        this.is = is;
        this.skullName = skullName;
    }

    public static Optional<ChestAttachments> getAttachmentByType(String type) {
        for (ChestAttachments value : values()) {
            if (value.name.equalsIgnoreCase(type)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public Optional<ChestAttachment> getExistingInstance(Map<String, Object> map, ChestInformation inf) {
        try {
            Method createFromSectionMethod = attachmentClass.getDeclaredMethod("createFromSection", Map.class, ChestInformation.class);

            return (Optional<ChestAttachment>) createFromSectionMethod.invoke(null, map, inf);
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load createFromSection for: " + attachmentClass.getName());
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<ChestAttachment> createNewInstance(ChestInformation inf) {
        try {
            Method createFromSectionMethod = attachmentClass.getDeclaredMethod("createNewInstance", String.class, ChestInformation.class);
            return Optional.of((ChestAttachment) createFromSectionMethod.invoke(null, name, inf));
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load createNewInstance for: " + attachmentClass.getName());
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public Pair<ItemStack, MxItemClicked> getAddItemStack(ChestInformation information) {

        MxItemClicked clicked = (mxInv, e) -> {
            information.addNewAttachment((Player) e.getWhoClicked(), this);
        };
        return new Pair<>(is, clicked);
    }

    public Class<? extends ChestAttachment> getAttachmentClass() {
        return attachmentClass;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getIs() {
        return is;
    }

    public String getSkullName() {
        return skullName;
    }
}
