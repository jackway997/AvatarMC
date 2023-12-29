package sprucegoose.avatarmc.region;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sprucegoose.avatarmc.abilities.Ability;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;


class SaberFactions extends RegionProtectionBase {

    protected SaberFactions() {
        super("Factions");
    }

    @Override
    public boolean isLocationBreakable(@NotNull Player player, @NotNull Location location) {
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation fLoc = new FLocation(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        final Faction faction = com.massivecraft.factions.Board.getInstance().getFactionAt(fLoc);
        final Relation relation = fPlayer.getRelationTo(faction);

        if ((faction.isWilderness() || fPlayer.getFaction().equals(faction) || relation == Relation.ALLY)) {
            return true;
        }
        player.sendMessage(ChatColor.ITALIC + "" +ChatColor.RED +"You can't do that in enemy land!");
        return false;

    }

    @Override
    public boolean isLocationPVPEnabled(@NotNull LivingEntity entity, @NotNull Location location) {
        return true;
    }
}

