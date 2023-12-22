package sprucegoose.avatarmc.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ItemMetaTag
{
    public static void setItemMetaTag(JavaPlugin plugin, ItemStack itemStack, String tagKey, String tagValue)
    {
            NamespacedKey key = new NamespacedKey(plugin, tagKey);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null)
            {
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tagValue);
                itemStack.setItemMeta(itemMeta);
            }
            else
                System.out.println("no item meta exists for object"+ itemStack.toString());
    }

    public static boolean itemStackHasTag(JavaPlugin plugin, ItemStack itemStack, String tagKey, String tagValue)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && tagValue != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            return container.has(key, PersistentDataType.STRING) &&
                    container.get(key, PersistentDataType.STRING).equals(tagValue);
        }
        else
            return false;
    }

    public static boolean itemStackHasAnyTag(JavaPlugin plugin, ItemStack itemStack, String tagKey)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null)
        {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            return container.has(key, PersistentDataType.STRING);
        }
        else
            return false;
    }

    public static String getItemStackTag(JavaPlugin plugin, ItemStack itemStack, String tagKey)
    {
        NamespacedKey key = new NamespacedKey(plugin, tagKey);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            if (container.has(key, PersistentDataType.STRING))
                return container.get(key, PersistentDataType.STRING);
            else
                return null;
        }
        else
            return null;
    }

    private final static String timeFormat = "yyyy/MM/dd HH:mm:ss";
    public static String setTimeStampedItemMeta(JavaPlugin plugin, String key, ItemStack item)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        setItemMetaTag(plugin, item, key, time);

        return time;
    }

    // returns number of seconds since timestamped metadata was set
    public static double getTimeElapsedEntityMeta(JavaPlugin plugin, ItemStack item, String timeStampKey)
    {
        String timestamp = getItemStackTag(plugin, item, timeStampKey);
        if (timestamp == null)
        {
            return -1;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime time = LocalDateTime.parse(timestamp, dtf);
        LocalDateTime timeNow = LocalDateTime.now();
        return Duration.between(time, timeNow).getSeconds();
    }
}
