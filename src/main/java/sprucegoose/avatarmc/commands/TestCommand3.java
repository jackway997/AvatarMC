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
import sprucegoose.avatarmc.abilities.water.WaterEffects;
import sprucegoose.avatarmc.region.RegionProtectionManager;

import javax.swing.plaf.synth.Region;

public class TestCommand3 implements CommandExecutor
{
    JavaPlugin plugin;
    RegionProtectionManager regionProtectionManager;
    public TestCommand3(JavaPlugin plugin, RegionProtectionManager regionProtectionManager)
    {
        this.plugin = plugin;
        this.regionProtectionManager = regionProtectionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
            System.out.println("breakable: "+ regionProtectionManager.isLocationBreakable(player, player.getLocation()));
            System.out.println("PVP: "+ regionProtectionManager.isLocationPVPEnabled(player, player.getLocation()));
        }

        return true;
    }


}
