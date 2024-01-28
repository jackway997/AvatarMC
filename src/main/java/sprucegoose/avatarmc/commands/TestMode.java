package sprucegoose.avatarmc.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;
import sprucegoose.avatarmc.utils.TestManager;

public class TestMode implements CommandExecutor
{
    JavaPlugin plugin;
    TestManager testManager;
    public TestMode(JavaPlugin plugin, TestManager testManager)
    {
        this.plugin = plugin;
        this.testManager = testManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        Player target;
        if (strings.length == 1)
        {
            target = plugin.getServer().getPlayer(strings[0]);
        }
        else if (sender instanceof Player player)
        {
            target = player;
        }
        else
        {
            plugin.getLogger().info("Invalid use of command");
            return true;
        }

        if (target != null)
        {
            if(!testManager.playerInTestMode(target))
            {
                testManager.setTestMode(target);
                sender.sendMessage(target.getName() + " is now in test mode");
            }
            else
            {
                testManager.removeTestMode(target);
                sender.sendMessage(target.getName() + " is no longer in test mode");
            }
        }
        else
        {
            plugin.getLogger().info("Invalid use of command");
        }
        return true;
    }
}
