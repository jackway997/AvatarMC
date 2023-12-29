package sprucegoose.avatarmc.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sprucegoose.avatarmc.abilities.Ability;

public abstract class RegionProtectionBase {

    private String plugin;
    private String path;
    private JavaPlugin cachedPlugin;

    public RegionProtectionBase(String plugin) {
        this(plugin, "Respect" + plugin);
    }

    public RegionProtectionBase(String plugin, String path) {
        this.plugin = plugin;
        this.path = path;

        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            this.cachedPlugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(plugin);
        }
    }

//    public final boolean isRegionProtected(@NotNull Player player, @NotNull Location location, @Nullable Ability ability) {
//
//            final boolean allowHarmless = false;
//
//            if (ability == null && allowHarmless) {
//                return false;
//            }
//            return isRegionProtectedReal(player, location, ability);
//    }

    //protected abstract boolean isRegionProtected(@NotNull Player player, @NotNull Location location, Ability ability);

    public abstract boolean isLocationBreakable(@NotNull Player player, @NotNull Location location);

    public abstract boolean isLocationPVPEnabled(@NotNull LivingEntity entity, @NotNull Location location);
}
