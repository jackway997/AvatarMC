package sprucegoose.avatarmc.abilities.earth;

import it.unimi.dsi.fastutil.ints.IntSets;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.abilities.AbilityUtil;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

public class EarthPrison extends Ability
{

    private long cooldown;
    private double range;
    private double hitRadius;
    private int height;
    private long standTime;
    private long crumbleTime;

    // put in code to deal with grass / flowers / passable nature
    private Map<UUID, BukkitRunnable> activeBends = new HashMap<>();
    private String blockDataKey = "EarthPrison";

    public EarthPrison(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.expert);
        setCooldown(cooldown * 1000);
    }

    @Override
    public void loadProperties()
    {
        this.cooldown = getConfig().getLong("Abilities.Earth.EarthPrison.Cooldown");
        this.range = getConfig().getDouble("Abilities.Earth.EarthPrison.Range");
        this.hitRadius = getConfig().getDouble("Abilities.Earth.EarthPrison.HitRadius");
        this.height = getConfig().getInt("Abilities.Earth.EarthPrison.Height");
        this.standTime = getConfig().getLong("Abilities.Earth.EarthPrison.StandTime");
        this.crumbleTime = getConfig().getLong("Abilities.Earth.EarthPrison.CrumbleTime");
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
        lore.add(ChatColor.GRAY + "Do not pass go, do");
        lore.add(ChatColor.GRAY + "not collect $200.");
        return getAbilityItem(plugin, player, lore, 2);
    }

    private boolean doAbility(Player player)
    {
        LivingEntity target = AbilityUtil.getHostileLOS(player, range, hitRadius);

        // don't execute ability if either player is in a PVP disabled zone, or no target is found.
        if (    target == null || !regProtManager.isLocationPVPEnabled(player, player.getLocation()) ||
                !regProtManager.isLocationPVPEnabled(player, target.getLocation()))
        {
            return false;
        }
        Location targetLoc = target.getLocation().getBlock().getLocation();

        //System.out.println("targeting: "+ target);
        Set<Location> prisonLocs = new HashSet<>();

        prisonLocs.add(targetLoc.clone().add(1,-1,0));
        prisonLocs.add(targetLoc.clone().add(-1,-1,0));
        prisonLocs.add(targetLoc.clone().add(0,-1,1));
        prisonLocs.add(targetLoc.clone().add(0,-1,-1));

        Set<Location> prisonLocs2 = new HashSet<>();
        // check for height differences in prison pillar locations
        for (Location loc : prisonLocs)
        {
            boolean valid = AbilityUtil.getLocationUpDown(loc); // updates the loc by reference
            if (valid)
            {
                prisonLocs2.add(loc);
            }
        }
        if (prisonLocs2.isEmpty())
            return false;

        // teleport target to the centre of the current block they're on
        Location centredLocation = target.getLocation().getBlock().getLocation().clone().add(0.5,0,0.5);
        centredLocation.setDirection(target.getLocation().getDirection());
        target.teleport(centredLocation);

        for (Location loc : prisonLocs2)
        {
            buildPrisonPillar(loc,height);
        }

        regProtManager.tagEntity(target, player);

        return true;
    }

    private void buildPrisonPillar(Location location, int height)
    {
        // Parameters
        long standTimeAct = standTime * 20L;
        long crumbleTimeAct = crumbleTime * 20L;

        // Set and remove item meta for base (foundation block)
        Block baseBlock = location.getBlock();
        BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, baseBlock);
        BukkitRunnable removeBaseBlockMetaTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if(BlockUtil.blockHasMeta(baseBlock, blockDataKey))
                {
                    BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, baseBlock);
                }
            }
        };
        removeBaseBlockMetaTask.runTaskLater(plugin,
                standTimeAct + crumbleTimeAct);

        // spawn pillar and schedule deconstruction
        for (int ii = 0 ; ii < height ; ii++)
        {
            location.add(0,1,0);
            Block block = location.getBlock();
            if (AbilityUtil.blockReplaceable(block))
            {
                Material previousMaterial = block.getType();
                block.setType(Material.POINTED_DRIPSTONE);
                BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, block);

                BukkitRunnable removePillarBlockTask = new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if(BlockUtil.blockHasMeta(block, blockDataKey))
                        {
                            block.setType(previousMaterial);
                            BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, block);
                        }
                    }
                };
                removePillarBlockTask.runTaskLater(plugin,
                        standTimeAct + (long)((double)crumbleTimeAct * ((double)(height-ii)/(double)height)));
            }
            else
            {
                break;
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e)
    {
        Block block = e.getBlock();
        if (BlockUtil.blockHasMeta(block,blockDataKey))
        {
            e.setCancelled(true);
        }
    }

}


