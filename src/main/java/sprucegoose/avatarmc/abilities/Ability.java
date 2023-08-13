package sprucegoose.avatarmc.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.UUID;

public class Ability {

    public final HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    public final HashMap<UUID, Boolean> benders = new HashMap<UUID, Boolean>();
    protected long cooldown = 3000;
    protected JavaPlugin plugin;

    public Ability(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void setCooldown(long cooldown)
    {
        this.cooldown = cooldown;
    }

    public void addCooldown(Player player, ItemStack item)
    {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        //System.out.println("Cooldown added for "+ player.getName()); // debug info
        updateCooldownDisplay(player, item);
    }

    public boolean onCooldown(Player player)
    {
        Long lastTime = cooldowns.get(player.getUniqueId());
        if (lastTime == null)
            return false;
        else return (System.currentTimeMillis() - lastTime) < cooldown;
    }

    public void updateCooldownDisplay(Player player, ItemStack item)
    {
        Long lastTime = cooldowns.get(player.getUniqueId());
        if (lastTime == null)
            return;
        else if (System.currentTimeMillis() - lastTime < cooldown)
        {
            int numSeconds = (int)Math.ceil((double)(cooldown - System.currentTimeMillis() + lastTime) / 1000.0);
            // update text on item
            //System.out.println(numSeconds);
            item.setAmount(numSeconds);

            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskLater(plugin, () ->
            {
                updateCooldownDisplay(player, item);
            }, 20L);

        }
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        return null;
    }

    public void teachPlayer(Player player)
    {
        if (benders.getOrDefault(player.getUniqueId(),false))
            benders.put(player.getUniqueId(), true);
        else
            System.out.println("Player already learnt skill");
    }

}
