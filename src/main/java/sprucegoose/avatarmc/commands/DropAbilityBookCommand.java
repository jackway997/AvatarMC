package sprucegoose.avatarmc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.abilities.AbilityManager;

import java.util.UUID;

public class DropAbilityBookCommand implements CommandExecutor
{
    JavaPlugin plugin;
    AbilityManager abilityManager;

    public DropAbilityBookCommand(JavaPlugin plugin, AbilityManager abilityManager)
    {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        Entity entity;
        String ability;

        if(strings.length == 2)
        {
            entity = plugin.getServer().getEntity(UUID.fromString(strings[0]));
            ability = strings[1];
        }
        else
        {
            sender.sendMessage("Invalid use of command");
            return true;
        }

        if (entity instanceof LivingEntity)
        {
            if (!abilityManager.dropAbilityBook((LivingEntity) entity, ability))
            {
                plugin.getLogger().warning("Invalid ability. Valid abilities are " +
                        abilityManager.getAbilityStringList());
            }
        }
        else
        {
            sender.sendMessage("player not found");
        }
        return true;
    }
}