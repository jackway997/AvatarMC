package sprucegoose.avatarmc.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class PlayerIDs
{
    public final static String playerIDKey = "player-id";

    public static void setItemStackPlayerID(JavaPlugin plugin, ItemStack itemStack, Player player)
    {
        UUID uuid = player.getUniqueId();
        NamespacedKey key = new NamespacedKey(plugin, playerIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, new UUIDDataType() , uuid);
        itemStack.setItemMeta(itemMeta);
    }

    public static boolean itemStackHasPlayerID(JavaPlugin plugin, ItemStack itemStack, Player player)
    {
        UUID uuid = player.getUniqueId();
        UUIDDataType uuidType = new UUIDDataType();
        NamespacedKey key = new NamespacedKey(plugin, playerIDKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if(     container.has(key, uuidType) &&
                container.get(key, uuidType).equals(uuid) )
        { return true; }
        else
        { return false; }
    }
}
