package sprucegoose.avatarmc.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.jetbrains.annotations.NotNull;

class WorldGuard extends RegionProtectionBase implements Listener {

    protected WorldGuard() {
        super("WorldGuard");
    }

    @Override
    public boolean isLocationBreakable(@NotNull Player player, @NotNull org.bukkit.Location reallocation) {

        final Location location = BukkitAdapter.adapt(reallocation);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        StateFlag.State state = query.queryState(location, localPlayer, Flags.BUILD);
        return state == StateFlag.State.ALLOW || state == null;
    }


    public boolean isLocationPVPEnabled(@NotNull Player player, @NotNull org.bukkit.Location reallocation)
    {
    final Location location = BukkitAdapter.adapt(reallocation);
    LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

    RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    StateFlag.State state = query.queryState(location, localPlayer, Flags.PVP);
        return state == StateFlag.State.ALLOW || state == null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        //System.out.println("Flow");
        Block block = event.getToBlock();
        org.bukkit.Location loc = block.getLocation();

        Material mat = block.getType();
        if (!block.isEmpty()) {
            event.setCancelled(true);
        }
    }
}