package sprucegoose.avatarmc.abilities.water;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityUtil;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.BlockUtil;

import java.util.*;

public class IceSpike extends Ability
{

    private long cooldown;
//    private double range;
//    private double hitRadius;
//    private int height;
//    private long standTime;
//    private long crumbleTime;


    public IceSpike(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.expert);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
//        this.cooldown = getConfig().getLong("Abilities.Earth.EarthPrison.Cooldown");
//        this.range = getConfig().getDouble("Abilities.Earth.EarthPrison.Range");
//        this.hitRadius = getConfig().getDouble("Abilities.Earth.EarthPrison.HitRadius");
//        this.height = getConfig().getInt("Abilities.Earth.EarthPrison.Height");
//        this.standTime = getConfig().getLong("Abilities.Earth.EarthPrison.StandTime");
//        this.crumbleTime = getConfig().getLong("Abilities.Earth.EarthPrison.CrumbleTime");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        // Check if the player is holding the skill item
        if (slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player))
        {
            if (doAbility(e.getPlayer()))
            {
                addCooldown(player, item);
            }
            e.setCancelled(true);
        }
    }

    public ItemStack getAbilityItem (JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "That's gotta be an");
        lore.add(ChatColor.GRAY + "ouchy :(");
        return getAbilityItem(plugin, player, lore, 2);
    }

    private boolean doAbility(Player player)
    {
        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("player_ice_elemental_spikes").orElse(null);
        Location spawnLocation = player.getLocation();
        if(mob != null){
            // spawns mob
            ActiveMob rock = mob.spawn(BukkitAdapter.adapt(spawnLocation),1);

            // get mob as bukkit entity
            Entity entity = rock.getEntity().getBukkitEntity();
            return true;
        }
        else
        {
            return false;
        }

    }


}


