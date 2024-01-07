package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;

public class GivePlayerExp implements CommandExecutor
{
    JavaPlugin plugin;
    ProgressionManager progressionManager;
    public GivePlayerExp(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        long newExp;
        Player target;
        if (sender instanceof Player player && strings.length == 1) {
            target = player;
            newExp = Long.parseLong(strings[0]);
        } else if (strings.length == 2)
        {
            target = plugin.getServer().getPlayer(strings[0]);
            newExp = Long.parseLong(strings[1]);
        }
        else
        {
            plugin.getLogger().info("Invalid use of command");
            return true;
        }

        if (newExp != 0 && target != null)
        {
            progressionManager.addExp(target, newExp);
            sender.sendMessage("you granted "+ target + " "+ newExp +" bending exp.");
        }
        else {
            plugin.getLogger().info("Invalid use of command");
        }
        return true;
    }
}
