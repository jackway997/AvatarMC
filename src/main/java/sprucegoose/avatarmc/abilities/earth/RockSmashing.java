 package sprucegoose.avatarmc.abilities.earth;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.BlockUtil;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

 public class RockSmashing extends Ability
 {

    private Map<Player, Integer> interactCounts = new HashMap<>();
    public Map<Player, BukkitTask> resetTasks = new HashMap<>();
    long last = 0;
    BukkitRunnable twoSecondTimer;
    BukkitRunnable releaseTask;



    public RockSmashing(JavaPlugin plugin, RegionProtectionManager regProtManager)
    {
        super(plugin, regProtManager, ELEMENT_TYPE.earth, ABILITY_LEVEL.adept);
        setCooldown(3000);
    }


    public void rightClickTimer(Player p) {

        if (last > 0) {
            if (twoSecondTimer == null) {

                twoSecondTimer = new BukkitRunnable() {
                    @Override
                    public void run() {

                            twoSecondTimer = null;
                            Block targetBlock = p.getTargetBlock(null, 4);
                            if (targetBlock.getType() != Material.AIR && targetBlock.getType() != Material.BEDROCK && targetBlock.getType() != Material.WATER && targetBlock.getType() != Material.LAVA && targetBlock.getType() != Material.CAVE_AIR && targetBlock.getType() != Material.VOID_AIR) {
                                // Store blocks to break in a list
                                List<Block> blocksToBreak = new ArrayList<>();

                                Block centerBlock = targetBlock;
                                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                                    for (int yOffset = -1; yOffset <= 1; yOffset++) {
                                        for (int zOffset = -1; zOffset <= 1; zOffset++) {
                                            Block blockGrid = centerBlock.getRelative(xOffset, yOffset, zOffset);
                                            if (blockGrid.getType() != Material.AIR) {
                                                blocksToBreak.add(blockGrid);

                                            }
                                        }
                                    }


                                // Break all stored blocks
                                for (Block block : blocksToBreak) {
                                    if (block.getType() != Material.BEDROCK && block.getType() != Material.WATER && block.getType() != Material.LAVA && block.getType() != Material.CAVE_AIR && block.getType() != Material.VOID_AIR && block.getType() != Material.OBSIDIAN) {
                                        block.breakNaturally();
                                    }
                                }
                            }
                        }
                        p.getInventory().removeItem(new ItemStack(Material.COAL, 1));

                    }


                    //this doesnt work for some reason this.cancel();


                };
                twoSecondTimer.runTaskLater(plugin, 40);

            }

            if ((System.currentTimeMillis() - last) <= 300) {
                if (releaseTask != null && getServer().getScheduler().isQueued(releaseTask.getTaskId())) {
                    getServer().getScheduler().cancelTask(releaseTask.getTaskId());
                }
                releaseTask = new BukkitRunnable() { //this way we can reference it above line 22 and set twoSecondTimer to null
                    @Override
                    public void run() {
                        if (twoSecondTimer != null && getServer().getScheduler().isQueued(twoSecondTimer.getTaskId())) {
                            getServer().getScheduler().cancelTask(twoSecondTimer.getTaskId());
                            twoSecondTimer = null; //we can do this because we referenced it above (line 22)


                        }

                        //  twoSecondTimer.cancel();
                        // this.cancel();


                    }
                };
                releaseTask.runTaskLater(plugin, 20);
            }

        }
        last = System.currentTimeMillis();

    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        Player player = e.getPlayer();
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        ItemStack coal = new ItemStack(Material.COAL);

        // Check if the player is holding the Earth item
        if (    slot != null && item != null &&
                (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null) &&
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player))
        {
            if (player.getInventory().containsAtLeast(coal, 1))
            {
                int count = interactCounts.getOrDefault(player, 0); // Get the current count for the player, defaulting to 0 if not present

                if (count < 3)
                {
                    count++; // Increment the interaction count
                    resetCountAfterDelay(player);
                    interactCounts.put(player, count); // Update the count for the player
                }

                if (count >= 3)
                {
                    rightClickTimer(player);
                   // e.setCancelled(true);
                } else
                {
                    resetCountAfterDelay(player);
                }
            }else
            {
                player.sendMessage(ChatColor.RED +"This ability requires coal in your inventory");
            }
        }
    }



    public void resetCountAfterDelay(Player p) {
        BukkitTask task = resetTasks.get(p);
        if (task != null && getServer().getScheduler().isQueued(task.getTaskId())) {
            getServer().getScheduler().cancelTask(task.getTaskId());
        }

        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            interactCounts.put(p, 0);
            resetTasks.remove(p);
        }, 14L);
        resetTasks.put(p, task);
    }



    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "That rock is");
        lore.add(ChatColor.GRAY + "looking at me funny");
        return getAbilityItem(plugin, player, lore, 2);
    }
}
