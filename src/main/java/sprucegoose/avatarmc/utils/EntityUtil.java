package sprucegoose.avatarmc.utils;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class EntityUtil
{
    private final static String timeFormat = "yyyy/MM/dd HH:mm:ss";
    public static String setTimeStampedEntityMeta(JavaPlugin plugin, String key, Entity entity)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        entity.setMetadata(key, new FixedMetadataValue(plugin, time));

        return time;
    }

    // returns number of seconds since timestamped metadata was set
    public static double getTimeElapsedEntityMeta(Entity entity, String timeStampKey)
    {
        List<MetadataValue> values = entity.getMetadata(timeStampKey);
        if (values.isEmpty())
        {
            return -1;
        }
        String timeStr = values.get(0).asString();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime time = LocalDateTime.parse(timeStr, dtf);
        LocalDateTime timeNow = LocalDateTime.now();
        return Duration.between(time, timeNow).getSeconds();
    }

    public static void removeTimeStampedEntityMeta(JavaPlugin plugin, Entity entity, String key)
    {
        entity.removeMetadata(key, plugin);
    }

    public static boolean entityHasMetaAtTime(Entity entity, String key, String time)
    {
        List<MetadataValue> values = entity.getMetadata(key);
        return !values.isEmpty() && values.get(0).asString().equals(time);
    }

    public static boolean entityHasMeta(Entity entity, String key)
    {
        return entity.hasMetadata(key);
    }

    public static void setEntityMeta(JavaPlugin plugin, Entity entity, String tagKey, String tagValue)
    {
        entity.setMetadata(tagKey, new FixedMetadataValue(plugin, tagValue));
    }

    public static boolean EntityHasTag(Entity entity, String key, String value)
    {
        List<MetadataValue> values = entity.getMetadata(key);
        return !values.isEmpty() && values.get(0).asString().equals(value);
    }


    public static String getEntityTag(Entity entity, String key)
    {
        List<MetadataValue> values = entity.getMetadata(key);
        if (!values.isEmpty())
        {
            return  values.get(0).asString();
        }
        else return null;
    }
}
