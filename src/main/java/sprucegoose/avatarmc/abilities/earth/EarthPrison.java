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



    // put in code to deal with grass / flowers / passable nature
    private Map<UUID, BukkitRunnable> activeBends = new HashMap<>();
    private String blockDataKey = "EarthPrison";

    public EarthPrison(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.expert);
        setCooldown(2000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();

        // Check if the player is holding the skill item
        if (slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin, item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player))
        {
            doAbility(e.getPlayer());
            addCooldown(player,item);
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

    private void doAbility(Player player)
    {
        System.out.println("doing ability");
        LivingEntity target = AbilityUtil.getHostileLOS(player, 24.0, 0.5);
        if (target == null)
            return;

        Location targetLoc = target.getLocation().getBlock().getLocation();

        System.out.println("targeting: "+ target);
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
            return;

        // teleport target to the centre of the current block they're on
        Location centredLocation = target.getLocation().getBlock().getLocation().clone().add(0.5,0,0.5);
        centredLocation.setDirection(target.getLocation().getDirection());
        target.teleport(centredLocation);

        for (Location loc : prisonLocs2)
        {
            buildPrisonPillar(loc,6);
        }
    }

    private void buildPrisonPillar(Location location, int height)
    {
        // Parameters
        long initialStandTime = 5L * 20L;
        long crumbleTime = 2L * 20L;

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
                initialStandTime + crumbleTime);

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
                        initialStandTime + (long)((double)crumbleTime * ((double)(height-ii)/(double)height)));
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


