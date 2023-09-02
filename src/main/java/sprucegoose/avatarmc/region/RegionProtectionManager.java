package sprucegoose.avatarmc.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sprucegoose.avatarmc.abilities.Ability;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getServer;

public class RegionProtectionManager
{
    public RegionProtectionManager(JavaPlugin plugin)
    {

        if (enabled("WorldGuard"))
        {
            WorldGuard wg =new WorldGuard();
            registerRegionProtection((JavaPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard"), wg);
            System.out.println("World Guard Registered!!!!");
            getServer().getPluginManager().registerEvents(wg, plugin);
        }
        if (enabled("Factions"))
        {
            registerRegionProtection((JavaPlugin) Bukkit.getPluginManager().getPlugin("Factions"),
                    new SaberFactions());
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

    public boolean isRegionProtected(Player player, Location location, Ability ability) {
        if (location != null && checkAll(player, location, ability)) return true;

        return checkAll(player, player.getLocation(), ability);
    }

    private boolean checkAll(Player player, Location location, Ability ability) {
        for (RegionProtectionBase protection : this.getActiveProtections().values()) {
            try {
                if (protection.isRegionProtected(player, location, ability)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean enabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

}
