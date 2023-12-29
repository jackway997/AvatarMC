package sprucegoose.avatarmc.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
        if (state == StateFlag.State.ALLOW || state == null) {
            return true;
        } else {
            player.sendMessage("You can't do that in this area!");
            return false;
        }
    }

    public boolean isLocationPVPEnabled(@NotNull LivingEntity entity, @NotNull org.bukkit.Location reallocation) {
        final Location location = BukkitAdapter.adapt(reallocation);

        RegionAssociable associable = createRegionAssociable(entity);
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        StateFlag.State state = query.queryState(location, associable, Flags.PVP);
        if (state == StateFlag.State.ALLOW || state == null)
        {
            return true;
        }
        else
        {
            if (entity instanceof Player player)
            {
                player.sendMessage("You can't do that in this area!");
            }
            return false;
        }
    }

    private RegionAssociable createRegionAssociable(Object cause)
    {
        if (cause instanceof Player)
        {
            return WorldGuardPlugin.inst().wrapPlayer((Player) cause);
        }
        else if (cause instanceof Entity entity)
        {
            RegionQuery query = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            WorldConfiguration config = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(entity.getWorld()));
            org.bukkit.Location loc = entity.getLocation(); // getOrigin() can be used on Paper if present
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc), config.useMaxPriorityAssociation);
        }
        else if (cause instanceof Block block)
        {
            RegionQuery query = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            WorldConfiguration config = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(block.getWorld()));
            org.bukkit.Location loc = block.getLocation();
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc), config.useMaxPriorityAssociation);
        }
        else
        {
            return Associables.constant(Association.NON_MEMBER);
        }
    }
}