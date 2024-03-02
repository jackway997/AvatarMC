package sprucegoose.avatarmc.abilities.earth;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.BlockUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RockFall extends Ability implements Listener
{
        private long cooldown;
        private int range;

        public RockFall(JavaPlugin plugin, RegionProtectionManager regProtMan)
        {
            super(plugin, regProtMan, ELEMENT_TYPE.earth, ABILITY_LEVEL.beginner);
            setCooldown(cooldown * 1000);
        }

        @Override
        public void loadProperties()
        {
            this.cooldown = getConfig().getLong("Abilities.Earth.RockFall.Cooldown");
            this.range = getConfig().getInt("Abilities.Earth.RockFall.Range");
        }

        @EventHandler
        public void onPlayerInteractEvent(PlayerInteractEvent e)
        {
            EquipmentSlot slot = e.getHand();
            ItemStack item = e.getItem();
            Player player = e.getPlayer();

            if (    slot != null && item != null &&
                    (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                    (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                    abilityChecks(player, item) && !onCooldown(player)
            )
            {
                addCooldown(player, item);
                doAbility(player);
                e.setCancelled(true);
            }
        }

        public void doAbility(Player player)
        {
            Block block = BlockUtil.getTargetBlock(player, 15);
            plugin.getLogger().info(block.toString());
            while(block != null  && block.isPassable())
            {
                block = block.getRelative(BlockFace.DOWN,1);
            }

            //if (block == null) {return;}

            Location location = block.getLocation().add(0.5,1,0.5);

            MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("golem_bullet_rock").orElse(null);
            Location spawnLocation = location;
            if(mob != null){
                // spawns mob
                ActiveMob rock = mob.spawn(BukkitAdapter.adapt(spawnLocation),1);

                // get mob as bukkit entity
                Entity entity = rock.getEntity().getBukkitEntity();
            }
        }

        public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
        {
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Rock test skill");
            return getAbilityItem(plugin, player, lore, 1);
        }
    }