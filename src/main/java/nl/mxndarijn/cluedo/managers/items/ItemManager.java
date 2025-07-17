package nl.mxndarijn.cluedo.managers.items;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.cluedo.items.Items;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ItemManager {

    private static ItemManager instance;

    private final ArrayList<MxItem> items;

    private ItemManager() {
        Logger.logMessage(LogLevel.INFORMATION, Prefix.ITEM_MANAGER, "Loading Item-Manager...");

        items = new ArrayList<>();
        for (Items item : Items.values()) {
            try {
                Logger.logMessage(LogLevel.DEBUG, Prefix.ITEM_MANAGER, "Loading usable item " + item.getClassObject().getName() + "...");
                MxItem mxItem = item.getClassObject().getDeclaredConstructor(
                        ItemStack.class,
                        MxWorldFilter.class,
                        boolean.class,
                        Action[].class
                ).newInstance(item.getItemStack(), item.getWorldFilter(), item.isGameItem(), item.getActions());
                items.add(mxItem);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                Logger.logMessage(LogLevel.ERROR, Prefix.ITEM_MANAGER, "Could not load MxItem: " + item.getClassObject().getName());
                e.printStackTrace();
            }
        }

    }

    public static ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }
}
