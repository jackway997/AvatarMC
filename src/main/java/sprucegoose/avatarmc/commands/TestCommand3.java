package sprucegoose.avatarmc.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.abilities.AbilityManager;
import sprucegoose.avatarmc.abilities.ProgressionManager;
import sprucegoose.avatarmc.abilities.water.WaterEffects;
import sprucegoose.avatarmc.region.RegionProtectionManager;

import javax.swing.plaf.synth.Region;

public class TestCommand3 implements CommandExecutor
{
    JavaPlugin plugin;
    ProgressionManager progressionManager;
    public TestCommand3(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        long newExp;
        if (strings.length == 1) {
            newExp = Long.parseLong(strings[0]);
        } else {
            plugin.getLogger().info("Invalid use of command");
            return true;
        }

        if (sender instanceof Player player)
        {
            plugin.getLogger().info("pre player exp: " + String.valueOf(progressionManager.getExp(player)));
            progressionManager.addExp(player, newExp);
            plugin.getLogger().info("post player exp: " + String.valueOf(progressionManager.getExp(player)));
        }
        else {
            plugin.getLogger().info("Invalid use of command");
        }
        return true;
    }
}
