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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import sprucegoose.avatarmc.storage.AbilityStorage;
import sprucegoose.avatarmc.utils.ItemMetaTag;

import java.util.*;

import static org.bukkit.event.inventory.ClickType.SHIFT_RIGHT;

public class AbilityManager implements Listener
{
    private final Set<Ability> abilities = new HashSet<>();
    private final JavaPlugin plugin;
    private final AbilityStorage abilityStorage;
    private final HashMap<String, Ability> abilityMatrix = new HashMap<>();
    private final ProgressionManager progressionManager;

    public AbilityManager(JavaPlugin plugin, AbilityStorage abilityStorage, ProgressionManager progressionManager)
    {
        this.plugin = plugin;
        this.abilityStorage = abilityStorage;
        this.progressionManager = progressionManager;
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

    public void removeAbility(Player player, Ability ability) {
        if(this.abilities.contains(ability)) {
            abilityStorage.removeAbility(player.getUniqueId(), ability.getAbilityID(), false);
        }
    }

    public boolean canBend(ProgressionManager.BENDER_TYPE bender, Ability.ELEMENT_TYPE element)
    {
        if (bender != null && element != null) {
            return ((bender == ProgressionManager.BENDER_TYPE.avatar) || (element.name().equals(bender.name())));
        }
        else return false;
    }

    public boolean removeAbility(Player player, String ability) {
        if (abilityMatrix.containsKey(ability))
        {
            abilityStorage.removeAbility(player.getUniqueId(), ability, false);
            return true;
        }
        else return false;
    }

    public boolean giveAbility(Player player, Ability ability)
    {
        if(this.abilities.contains(ability))
        {
            abilityStorage.giveAbility(player.getUniqueId(), ability.getAbilityID(), false);
            return true;
        }
        else return false;
    }

    public boolean giveAbility(Player player, String ability) {
        if (abilityMatrix.containsKey(ability))
        {
            abilityStorage.giveAbility(player.getUniqueId(), ability, false);
            return true;
        }
        else return false;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e)
    {
        this.abilityStorage.load(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e)
    {
        this.abilityStorage.unload(e.getPlayer().getUniqueId());
    }

    public String getAbilityStringList()
    {
        StringBuilder output = new StringBuilder("[");
        for (Ability ability : abilities)
        {
            output.append(ability.getAbilityID()).append(", ");
        }
        output.delete(output.length()-2, output.length());
        output.append("]");
        return output.toString();
    }

    public Set<Ability> getPlayerAbilities(Player player)
    {
        Set<Ability> outSet = new HashSet<>();

        for (String ability : abilityStorage.getAbilities(player.getUniqueId()))
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
        Inventory inventory = e.getClickedInventory();


        if (inventory != null && inventory.contains(item) &&
                ItemMetaTag.itemStackHasAnyTag(plugin, item, Ability.getSkillBookKey())
                && e.getClick() == SHIFT_RIGHT)
        {
            e.setCancelled(true);
            readSkillBook(player, item, inventory);
        }
    }
    @EventHandler
    public void useSkillBookHandListener(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();
        Inventory inventory = player.getInventory();

        if (    slot != null && inventory.contains(item) &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && // right click
                (slot.equals(EquipmentSlot.HAND)  && player.isSneaking() &&
                ItemMetaTag.itemStackHasAnyTag(plugin, item, Ability.getSkillBookKey())))
        {
            e.setCancelled(true);
            readSkillBook(player, item, inventory);
        }
    }

    private void readSkillBook(Player player, ItemStack item, Inventory inventory)
    {
        for (Ability ability : abilities) //check which skill book is read
        {
            if (ItemMetaTag.itemStackHasTag(plugin, item, Ability.getSkillBookKey(), ability.getAbilityBookID()))
            {
                if (canBend(progressionManager.getBenderType(player), ability.getElement()))
                {

                    if (!getPlayerAbilities(player).contains(ability)) {
                        if (item.getAmount() > 1)
                            item.setAmount(item.getAmount() - 1);
                        else
                            inventory.remove(item);

                        giveAbility(player, ability);
                        player.sendMessage("You learnt " + ability + "!");
                    } else {
                        player.sendMessage("You already know " + ability + "!");
                    }
                }
                else
                {
                    player.sendMessage("You cannot learn "+ ability.getElement() + " skills.");
                }
                break;

            }
        }
    }
}
