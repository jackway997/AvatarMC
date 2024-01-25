package sprucegoose.avatarmc.commands;

import com.massivecraft.factions.shade.net.kyori.adventure.platform.facet.Facet;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.ProgressionManager;

public class ChangeBenderType implements CommandExecutor
{
    JavaPlugin plugin;
    ProgressionManager progressionManager;
    public ChangeBenderType(JavaPlugin plugin, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        String newType;
        String confirm;
        if (strings.length == 2)
        {
            newType = strings[0];
            confirm = strings[1];
        }
        else if(strings.length == 1)
        {
            sender.sendMessage(ChatColor.RED + "Please use "+ ChatColor.YELLOW + "/change_bender_type <new_type> confirm"+ ChatColor.RED + " to confirm your selection!");
            return true;
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (sender instanceof Player player && confirm.equalsIgnoreCase("confirm"))
        {
            plugin.getLogger().info(ChatColor.GREEN + ""+ ChatColor.ITALIC + player.getName() +"became a "+ newType +" bender!");
            progressionManager.reassignBenderType(player, ProgressionManager.stringToBenderType(newType));
            sender.sendMessage(ChatColor.GREEN + ""+ ChatColor.ITALIC +"You became a "+ newType +" bender!");
        }
        else {
            plugin.getLogger().info("Invalid use of command");
        }
        return true;
    }
}
