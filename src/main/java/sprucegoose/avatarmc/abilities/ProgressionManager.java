package sprucegoose.avatarmc.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.storage.ProgressionStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProgressionManager {

    public enum BENDER_TYPE{air, water, earth, fire, avatar}

    private final JavaPlugin plugin;
    private final ProgressionStorage progressionStorage;

    public ProgressionManager(JavaPlugin plugin, ProgressionStorage progressionStorage) {
        this.plugin = plugin;
        this.progressionStorage = progressionStorage;
    }

    public static BENDER_TYPE stringToBenderType(String input)
    {
        if (input != null) {
            return switch (input) {
                case "air" -> BENDER_TYPE.air;
                case "water" -> BENDER_TYPE.water;
                case "earth" -> BENDER_TYPE.earth;
                case "fire" -> BENDER_TYPE.fire;
                case "avatar" -> BENDER_TYPE.avatar;
                default -> null;
            };
        } else return null;

    }

    public String getBenderTypeStringList()
    {
        StringBuilder output = new StringBuilder("[");
        for (BENDER_TYPE type : BENDER_TYPE.values())
        {
            output.append(type).append(", ");
        }
        output.delete(output.length()-2, output.length());
        output.append("]");
        return output.toString();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        this.progressionStorage.load(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e)
    {
        this.progressionStorage.unload(e.getPlayer().getUniqueId());
    }

    public BENDER_TYPE getBenderType(Player player)
    {
        return stringToBenderType(progressionStorage.getBenderType(player.getUniqueId()));
    }

    public boolean setBenderType(Player player, String type)
    {
        for(BENDER_TYPE benderType : BENDER_TYPE.values() )
        {
            if (type.equals(benderType.name()))
            {
                progressionStorage.setBenderType(player.getUniqueId(), type, false);
                return true;
            }
        }
        return false;
    }

    public boolean setBenderType(Player player, BENDER_TYPE type)
    {
        for(BENDER_TYPE benderType : BENDER_TYPE.values() )
        {
            if (type.equals(benderType))
            {
                progressionStorage.setBenderType(player.getUniqueId(), type.name(), false);
                return true;
            }
        }
        return false;
    }

    public void removeBenderType(Player player)
    {
        progressionStorage.removeBenderType(player.getUniqueId(), false);
    }

}
