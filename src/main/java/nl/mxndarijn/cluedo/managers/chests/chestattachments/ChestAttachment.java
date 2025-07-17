package nl.mxndarijn.cluedo.managers.chests.chestattachments;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.cluedo.game.Game;
import nl.mxndarijn.cluedo.game.GamePlayer;
import nl.mxndarijn.cluedo.managers.chests.ChestInformation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class ChestAttachment {
    public ChestInformation information;
    public String type;
    public Optional<ArmorStand> armorStand;
    private Optional<Game> game;

    public static boolean getDefaultValues(ChestAttachment attachment, ChestInformation information, Map<String, Object> section) {

        if (section == null) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load ChestAttachment (No Section)");
            return false;
        }
        if (!section.containsKey("type")) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load ChestAttachment (No Type)");
            return false;
        }
        attachment.type = (String) section.get("type");
        attachment.information = information;
        return true;
    }

    public void onGameStart(Game game) {
        this.game = Optional.of(game);
    }

    public void setDefaults(String type, ChestInformation information) {
        this.type = type;
        this.information = information;
    }

    public void onGamePause() {

    }

    public void onGameUpdate(long delta) {

    }


    public abstract Pair<ItemStack, MxItemClicked> getEditAttachmentItem();

    public boolean canOpenChest(GamePlayer gamePlayer) {
        return true;
    }

    public void onOpenChest(InventoryOpenEvent e) {

    }

    public void getDataDefaults(Map<String, Object> map) {
        map.put("type", type);
    }

    public abstract Map<String, Object> getDataForSaving();

    public void spawnArmorStand() {
        if (game.isEmpty())
            return;
        if (game.get().getMxWorld().isEmpty())
            return;
        Location location = information.getLocation().getLocation(Bukkit.getWorld(game.get().getMxWorld().get().getWorldUID())).add(0.5, 0.2, 0.5);

        armorStand = Optional.of((ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND));

        ArmorStand ar = armorStand.get();

        ar.setCustomNameVisible(false);
        ar.setSmall(true);
        ar.setArms(false);
        ar.setBasePlate(false);
        ar.setInvisible(true);
        ar.setInvulnerable(true);
        ar.setGravity(false);
        ar.customName(Component.text("attachment"));
        ar.setCollidable(false);
        ar.getEquipment().setHelmet(MxSkullItemStackBuilder.create(1).setSkinFromHeadsData(ChestAttachments.getAttachmentByType(type).get().getSkullName())
                .build());

        Location blockLocation = information.getLocation().getLocation(Bukkit.getWorld(game.get().getMxWorld().get().getWorldUID()));
        if (blockLocation.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Chest di) {
            BlockFace bf = di.getFacing();
            float val = switch (bf) {
                case NORTH -> 180f;
                case EAST -> -90f;
                case SOUTH -> 0f;
                case WEST -> 90f;
                default -> 0f;
            };
            ar.setHeadPose(new EulerAngle(0, Math.toRadians(val), 0));

            AttributeInstance attribute = ar.getAttribute(Attribute.ARMOR);

            if (attribute != null) {
                // Verkrijg het huidige waarde van DisabledSlots
                Collection<AttributeModifier> modifiers = attribute.getModifiers();

                // Verwijder eventuele bestaande modifiers voor DisabledSlots
                modifiers.removeIf(modifier -> modifier.getName().equals("DisabledSlots"));

                // Voeg een nieuwe modifier toe voor DisabledSlots
                int disabledSlotsValue = 4144959; // De waarde die je wilt instellen
//                attribute.addModifier(new AttributeModifier("DisabledSlots", disabledSlotsValue, AttributeModifier.Operation.ADD_NUMBER));

                // Zorg ervoor dat de ArmorStand wordt bijgewerkt om de wijzigingen door te voeren
            }
        }
    }

    public void onChestInteract(GamePlayer gamePlayer, PlayerInteractEvent e, Game game, Player p) {

    }

    public void onChestInventoryClick(GamePlayer gamePlayer, InventoryClickEvent e, Game game, Player p) {

    }
}
