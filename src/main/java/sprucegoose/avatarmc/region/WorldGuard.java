package sprucegoose.avatarmc.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.World;
import org.bukkit.entity.Player;
import sprucegoose.avatarmc.abilities.Ability;

class WorldGuard extends RegionProtectionBase {

    protected WorldGuard() {
        super("WorldGuard");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, org.bukkit.Location reallocation, Ability ability) {

        final Location location = BukkitAdapter.adapt(reallocation);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return !query.testState(location, localPlayer, Flags.BUILD);
    }
}
