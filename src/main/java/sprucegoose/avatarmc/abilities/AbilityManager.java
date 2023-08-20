package sprucegoose.avatarmc.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import sprucegoose.avatarmc.storage.PlayerProgression;
import sprucegoose.avatarmc.utils.ItemMetaTag;

import java.util.*;

import static org.bukkit.event.inventory.ClickType.SHIFT_RIGHT;

public class AbilityManager implements Listener
{
    private final Set<Ability> abilities = new HashSet<>();
    private final JavaPlugin plugin;
    private final PlayerProgression pp;
    private final HashMap<String, Ability> abilityMatrix = new HashMap<>();

    public AbilityManager(JavaPlugin plugin, PlayerProgression pp)
    {
        this.plugin = plugin;
        this.pp = pp;
        registerAbilities();
    }

    private void registerAbilities()
    {
        registerAbility(new CreateWater(plugin));
        registerAbility(new AirBlast(plugin));
        registerAbility(new BoulderToss(plugin));
    }

    private void registerAbility(Ability ability)
    {
        this.abilities.add(ability);
        this.abilityMatrix.put(ability.getClass().getSimpleName(), ability);
    }

    public Set<Ability> getAbilities()
    {
        return abilities;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        this.pp.load(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e)
    {
        this.pp.unload(e.getPlayer().getUniqueId());
    }

    public Set<Ability> getPlayerAbilities(Player player)
    {
        Set<Ability> outSet = new HashSet<>();

        for (String ability : pp.getAbilities(player))
        {
            if (abilityMatrix.containsKey(ability))
                outSet.add(abilityMatrix.get(ability));
            // else
            //    plugin.getLogger().warning("Player has ability:" + ability + " which does not exist!");
        }
        return outSet;
    }

    @EventHandler
    public void useSkillBookInventoryListener(InventoryClickEvent e)
    {
        Player player = (Player)e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item != null && ItemMetaTag.itemStackHasAnyTag(plugin, item, Ability.getSkillBookKey())
                && e.getClick() == SHIFT_RIGHT)
        {
            for (Ability ability : abilities)
            {
                if (ItemMetaTag.itemStackHasTag(plugin, item, Ability.getSkillBookKey(), ability.getAbilityBookID())) {
                    try
                    {
                        String abilityName = ability.getAbilityID();
                        if (!pp.getAbilities(player).contains(abilityName))
                        {
                            e.getClickedInventory().remove(item);
                            pp.giveAbility(player.getUniqueId(), abilityName, false);
                            player.sendMessage("You learnt " +abilityName + "!");
                            e.setCancelled(true);
                        }
                        else
                        {
                            player.sendMessage("You already know " +abilityName + "!");
                            e.setCancelled(true);
                        }
                        break;

                    } catch (NullPointerException exception)
                    {
                        plugin.getLogger().info("error with learning skill: skill book seen as null");
                    }
                }
            }
        }
    }

    @EventHandler
    public void useSkillBookHandListener(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (    slot != null && item != null &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && // right click
                (slot.equals(EquipmentSlot.HAND)  && player.isSneaking() &&
                ItemMetaTag.itemStackHasAnyTag(plugin, item, Ability.getSkillBookKey())))
        {
            for (Ability ability : abilities)
            {
                if (ItemMetaTag.itemStackHasTag(plugin, item, Ability.getSkillBookKey(), ability.getAbilityBookID())) {
                    try
                    {
                        String abilityName = ability.getAbilityID();
                        if (!pp.getAbilities(player).contains(abilityName))
                        {
                            player.getInventory().remove(item);
                            pp.giveAbility(player.getUniqueId(), abilityName, false);
                            player.sendMessage("You learnt " +abilityName + "!");
                            e.setCancelled(true);
                        }
                        else
                        {
                            player.sendMessage("You already know " +abilityName + "!");
                            e.setCancelled(true);
                        }
                        break;

                    } catch (NullPointerException exception)
                    {
                        plugin.getLogger().info("error with learning skill: skill book seen as null");
                    }
                }
            }
        }
    }
}
