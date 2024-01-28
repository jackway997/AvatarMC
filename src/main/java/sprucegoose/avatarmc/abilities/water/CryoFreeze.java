package sprucegoose.avatarmc.abilities.water;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CryoFreeze extends Ability implements Listener {
    String blockDataKey = "cryoFreeze";

    public CryoFreeze(JavaPlugin plugin, RegionProtectionManager regProtMan) {
        super(plugin, regProtMan, ELEMENT_TYPE.water, ABILITY_LEVEL.expert);
        setCooldown(10000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();

        Block targetBlockLocation = player.getTargetBlock(null, 100).getRelative(e.getBlockFace());

        if (slot != null && item != null &&
                e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                targetBlockLocation.isEmpty() &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                abilityChecks(player, item) && !onCooldown(player) /*&&
                !regProtManager.isRegionProtected(player, e.getClickedBlock().getLocation(),this)*/
        )
        {
            addCooldown(player, item);
            doAbility(player);
            e.setCancelled(true);
        }
    }

    private void doAbility(Player player) {
        System.out.println("doing ability");

        if (player == null)
            return;

        Location targetLoc = player.getLocation().getBlock().getLocation();

        System.out.println("targeting: " + player);
        Set<Location> prisonLocs = new HashSet<>();

        prisonLocs.add(targetLoc.clone().add(1, -1, 0));
        prisonLocs.add(targetLoc.clone().add(-1, -1, 0));
        prisonLocs.add(targetLoc.clone().add(0, -1, 1));
        prisonLocs.add(targetLoc.clone().add(0, -1, -1));
        //2nd level
//        prisonLocs.add(targetLoc.clone().add(0,0,-1));
//        prisonLocs.add(targetLoc.clone().add(-1,0,0));
//        prisonLocs.add(targetLoc.clone().add(0,0,1));
//        prisonLocs.add(targetLoc.clone().add(0,0,-1));
//        prisonLocs.add(targetLoc.clone().add(0,1,0));


        Set<Location> prisonLocs2 = new HashSet<>();
        // check for height differences in prison pillar locations
        for (Location loc : prisonLocs) {
            boolean valid = AbilityUtil.getLocationUpDown(loc); // updates the loc by reference
            if (valid) {
                prisonLocs2.add(loc);
            }
        }
        if (prisonLocs2.isEmpty())
            return;

        // teleport target to the centre of the current block they're on
        Location centredLocation = player.getLocation().getBlock().getLocation().clone().add(0.5, 0, 0.5);
        centredLocation.setDirection(player.getLocation().getDirection());
        player.teleport(centredLocation);

        for (Location loc : prisonLocs2) {
            cryoFreeze(loc.clone());
            cryoFreeze(loc.clone().add(0, 1, 0));
            cryoFreeze(loc.clone().add(0, 2, 0));
        }
        Location headBlock = targetLoc.clone().add(0, -1, 0);//gets their feet
        AbilityUtil.getLocationUpDown(headBlock); // updates the loc by reference
        //add 2 to the location of headBlock
        headBlock.add(0, 2, 0);
        Location headBlock2 = headBlock.clone().add(0, 1, 0);


        //call cryofreeze
        cryoFreeze(headBlock);
        cryoFreeze(headBlock2);


    }

    private void cryoFreeze(Location location) {
        long duration = 120L;

        // Set and remove item meta for base (foundation block)
        Block baseBlock = location.getBlock();
        BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, baseBlock); //marking all blocks below so they cant mine underneath
        BukkitRunnable removeBaseBlockMetaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (BlockUtil.blockHasMeta(baseBlock, blockDataKey)) {
                    BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, baseBlock);
                }
            }
        };
        removeBaseBlockMetaTask.runTaskLater(plugin,
                duration);

        // spawn pillar and schedule deconstruction

        location.add(0, 1, 0);
        Block block = location.getBlock();
        if (AbilityUtil.blockReplaceable(block)) {
            Material previousMaterial = block.getType();
            block.setType(Material.ICE);
            BlockUtil.setTimeStampedBlockMeta(plugin, blockDataKey, block);

            BukkitRunnable removePillarBlockTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (BlockUtil.blockHasMeta(block, blockDataKey)) {
                        System.out.println("removing meta");
                        block.setType(previousMaterial);
                        BlockUtil.removeTimeStampedBlockMeta(plugin, blockDataKey, block);
                    }
                }
            };
            removePillarBlockTask.runTaskLater(plugin, duration);

            //
        }

    }

    /*
        @EventHandler
        public void onBlockBreakEvent(BlockBreakEvent e)
        {
            Block block = e.getBlock();
            if (BlockUtil.blockHasMeta(block,blockDataKey))
            {
                e.setCancelled(true);
            }
        }

     */

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Access Denied");
        return getAbilityItem(plugin, player, lore, 2);
    }

}




