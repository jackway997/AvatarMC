package sprucegoose.avatarmc.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;
import com.massivecraft.factions.*;

public class RegionProtectionManager
{
    public RegionProtectionManager(JavaPlugin plugin)
    {

        if (enabled("WorldGuard"))
        {
            WorldGuard wg =new WorldGuard();
            registerRegionProtection((JavaPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard"), wg);
            System.out.println("World Guard Registered!");
            getServer().getPluginManager().registerEvents(wg, plugin);
        }
        if (enabled("Factions"))
        {
            registerRegionProtection((JavaPlugin) Bukkit.getPluginManager().getPlugin("Factions"),
                    new SaberFactions());
            System.out.println("Factions Registered!");
        }
    }

    private Map<JavaPlugin, RegionProtectionBase> PROTECTIONS = new LinkedHashMap<>(); //LinkedHashMap keeps the hashmap order of insertion

    public void registerRegionProtection( JavaPlugin plugin, RegionProtectionBase regionProtection) {
        PROTECTIONS.put(plugin, regionProtection);
    }

    public void unloadPlugin(JavaPlugin plugin) {
        PROTECTIONS.remove(plugin);
    }

    public Map<JavaPlugin, RegionProtectionBase> getActiveProtections() {
        return PROTECTIONS;
    }

    public boolean isLocationBreakable(Player player, Location location) {
        if (location != null && player != null);
        {
            return checkIsLocationBreakable(player, location);
        }
    }

    public boolean isLocationPVPEnabled(LivingEntity entity, Location location) {
        if (location != null && entity != null);
        {
            return checkIsLocationPVPEnabled(entity, location);
        }
    }

    private boolean checkIsLocationBreakable(Player player, Location location) {
        for (RegionProtectionBase protection : this.getActiveProtections().values()) {
            try {
                if (!protection.isLocationBreakable(player, location)) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean checkIsLocationPVPEnabled(LivingEntity entity, Location location) {
        for (RegionProtectionBase protection : this.getActiveProtections().values()) {
            try {
                if (!protection.isLocationPVPEnabled(entity, location)) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static boolean enabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

}
