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

public class TestCommand3 implements CommandExecutor
{
    JavaPlugin plugin;
    WaterEffects waterEffects;
    public TestCommand3(JavaPlugin plugin, WaterEffects waterEffects)
    {
        this.plugin = plugin;
        this.waterEffects = waterEffects;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
//            BukkitRunnable windForceTask = new BukkitRunnable()
//            {
//                @Override
//                public void run()
//                {
//                    player.setVelocity(player.getVelocity().add(new Vector(0.1, 0.1, 0)));
//                }
//            }; windForceTask.runTaskTimer(plugin, 0L, 1L);
//
//            BukkitRunnable cancelTask = new BukkitRunnable()
//            {
//                @Override
//                public void run()
//                {
//                      windForceTask.cancel();
//                }
//            };
//            cancelTask.runTaskLater(plugin, 5L*20L);


        }


        return true;
    }


}
