package sprucegoose.avatarmc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.storage.PlayerProgression;

public class TestCommand3 implements CommandExecutor
{
    JavaPlugin plugin;
    PlayerProgression pp;

    public TestCommand3(JavaPlugin plugin, PlayerProgression pp)
    {
        this.plugin = plugin;
        this.pp = pp;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (sender instanceof Player player)
        {
            pp.removeAbility(player.getUniqueId(),strings[0], false);

        }
        return true;
    }
}