package sprucegoose.avatarmc.abilities;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import sprucegoose.avatarmc.storage.ProgressionStorage;

public class ProgressionManager implements Listener {

    public enum BENDER_TYPE{air, water, earth, fire, avatar, none}
    private final JavaPlugin plugin;
    private final ProgressionStorage progressionStorage;
    private AbilityManager abilityManager;

    public ProgressionManager(JavaPlugin plugin, ProgressionStorage progressionStorage/*, AbilityManager abilityManager*/)
    {
        this.plugin = plugin;
        this.progressionStorage = progressionStorage;
        //this.abilityManager = abilityManager;
    }

    public void setAbilityManager(AbilityManager abilityManager)
    {
        this.abilityManager = abilityManager;
    }

    private long[] expLevels = {1, 500, 2000, 10000};

    public static BENDER_TYPE stringToBenderType(String input)
    {
        if (input != null) {
            return switch (input) {
                case "air" -> BENDER_TYPE.air;
                case "water" -> BENDER_TYPE.water;
                case "earth" -> BENDER_TYPE.earth;
                case "fire" -> BENDER_TYPE.fire;
                case "avatar" -> BENDER_TYPE.avatar;
                case "none" -> BENDER_TYPE.none;
                default -> null;
            };
        } else return null;
    }

    public static ChatColor getElementColor(BENDER_TYPE type)
    {
        return Ability.getElementColor(getAbilityElement(type));
    }

    private static Ability.ELEMENT_TYPE getAbilityElement(BENDER_TYPE type)
    {
        if (type != null) {
            return switch (type) {
                case air -> Ability.ELEMENT_TYPE.air;
                case water -> Ability.ELEMENT_TYPE.water;
                case earth -> Ability.ELEMENT_TYPE.earth;
                case fire -> Ability.ELEMENT_TYPE.fire;
                default -> null;
            };
        } else return null;
    }
    public String getBenderTypeStringList()
    {
        StringBuilder output = new StringBuilder("[");
        for (BENDER_TYPE type : BENDER_TYPE.values())
        {
            output.append(type).append(", ");
        }
        output.delete(output.length()-2, output.length());
        output.append("]");
        return output.toString();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        this.progressionStorage.load(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e)
    {
        plugin.getLogger().info("player leave event");
        this.progressionStorage.unload(e.getPlayer().getUniqueId());
    }

    public BENDER_TYPE getBenderType(Player player)
    {
        return stringToBenderType(progressionStorage.getBenderType(player.getUniqueId()));
    }

    public boolean setBenderType(Player player, String type)
    {
        for(BENDER_TYPE benderType : BENDER_TYPE.values() )
        {
            if (type.equals(benderType.name()))
            {
                progressionStorage.setBenderType(player.getUniqueId(), type, false);
                return true;
            }
        }
        return false;
    }

    public boolean reassignBenderType(Player player, BENDER_TYPE type)
    {
        BENDER_TYPE currentType = getBenderType(player);
        long newExp = getExp(player);

        if(currentType == type)
        {
            plugin.getLogger().info("can't reassign type to player that already has that bending type");
            return false;
        }

        if (currentType != null && currentType != BENDER_TYPE.none && newExp != 0 )
        {
            // half the players exp when they
            newExp = newExp / 2L;

        }
        if (newExp <= 1)
        {
            newExp = 1;
        }

        setBenderType(player, type);
        setExp(player, newExp);
        abilityManager.removeAllAbilities(player);
        return true;
    }

    public boolean setBenderType(Player player, BENDER_TYPE type)
    {
        progressionStorage.setBenderType(player.getUniqueId(), type.name(), false);
        return true;
    }

    public void removeBenderType(Player player)
    {
        progressionStorage.removeBenderType(player.getUniqueId(), false);
    }

    public void addExp(Player player, long exp)
    {
        BENDER_TYPE type = getBenderType(player);
        if (type != null && type != BENDER_TYPE.none)
        {
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You gained " + exp + " bending exp!");
            checkLevelUp(player, exp);
            progressionStorage.addExp(player.getUniqueId(), exp);

        }
    }

    private boolean checkLevelUp(Player player, long exp)
    {
        Ability.ABILITY_LEVEL level = getBenderLevel(player);
        long currExp = getExp(player);
        if (level == Ability.ABILITY_LEVEL.master)
        {
            return false;
        }
        int index = level.ordinal();
        long expGoal = expLevels[index+1];
        if (currExp + exp >= expGoal) // if the player has levelled up
        {
            BENDER_TYPE type = getBenderType(player);

            // assume type not none or null
            if (type == BENDER_TYPE.avatar) {
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Congratulations, you levelled up to " +
                        Ability.ABILITY_LEVEL.values()[index + 1] + " " + getBenderType(player) +"!");
            }
            else
            {
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Congratulations, you levelled up to " +
                        Ability.ABILITY_LEVEL.values()[index + 1] + " " + getBenderType(player) +" bender!");
            }
            return true;
        }
        return false;
    }

    private String getExpProgressionStr(@NotNull Player player)
    {
        long exp = getExp(player);
        Ability.ABILITY_LEVEL level = getBenderLevel(player);
        int index = level.ordinal();
        if (level != Ability.ABILITY_LEVEL.master)
        {
            long expGoal = expLevels[index+1];
            return level + " ["+ exp +"/"+ expGoal +"]";
        }
        else
        {
            return level + " ["+ exp + "]";
        }
    }

    public void setExp(Player player, long exp)
    {
        progressionStorage.setExp(player.getUniqueId(), exp);
    }

    public Long getExp(Player player)
    {
        return progressionStorage.getExp(player.getUniqueId());
    }

    public Ability.ABILITY_LEVEL getBenderLevel(Player player)
    {
        long exp = progressionStorage.getExp(player.getUniqueId());

        if      (exp >= expLevels[3]) return Ability.ABILITY_LEVEL.master;
        else if (exp >= expLevels[2]) return Ability.ABILITY_LEVEL.expert;
        else if (exp >= expLevels[1]) return Ability.ABILITY_LEVEL.adept;
        else return Ability.ABILITY_LEVEL.beginner;
    }

    public void onDisable()
    {
        progressionStorage.unloadAll();

    }
}
