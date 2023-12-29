package sprucegoose.avatarmc.abilities.water;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import sprucegoose.avatarmc.abilities.Ability;
import sprucegoose.avatarmc.region.RegionProtectionManager;
import sprucegoose.avatarmc.utils.AvatarIDs;
import sprucegoose.avatarmc.utils.ImageParticles;
import sprucegoose.avatarmc.utils.ItemMetaTag;
import sprucegoose.avatarmc.utils.PlayerIDs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.event.inventory.ClickType.SHIFT_RIGHT;

public class IceBlade extends WaterAbility implements Listener
{
    // use wooden sword with damage modifier
    // keep track of active casters, and listen for attack events. If the player attacks with the weapon
    // after the timer has expired, the weapon shatters
    // shareable for now.
    // Make the skill shift right clickable so that can be used in inventory.

    public IceBlade(JavaPlugin plugin, RegionProtectionManager regProtMan, WaterEffects waterEffects)
    {
        super(plugin, regProtMan, waterEffects, ABILITY_LEVEL.adept);
        setCooldown(10000);
    }

    // constants
    private final String skillKey = "IceBlade";
    private final int expirationTime = 10; // seconds

    @EventHandler
    public void useSkillFromHand(PlayerInteractEvent e)
    {
        EquipmentSlot slot = e.getHand();
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();
        Inventory inventory = player.getInventory();

        if (    slot != null && inventory.contains(item) &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && // right click
                (slot.equals(EquipmentSlot.HAND) || slot.equals(EquipmentSlot.OFF_HAND)) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) && player.isSneaking() &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player))
        {
            e.setCancelled(true);
            createBlade(plugin, player);
            addCooldown(player, item);
        }
    }

    @EventHandler
    public void useSkillFromInventory(InventoryClickEvent e)
    {
        Player player = (Player)e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        Inventory inventory = e.getClickedInventory();


        if (inventory != null && inventory.contains(item) && (e.getClick() == SHIFT_RIGHT) &&
                AvatarIDs.itemStackHasAvatarID(plugin,item, this.getAbilityID()) &&
                PlayerIDs.itemStackHasPlayerID(plugin, item, player) && !onCooldown(player))
        {
            e.setCancelled(true);
            createBlade(plugin, player);
            addCooldown(player, item);
        }
    }

    private void createBlade(JavaPlugin plugin, Player player)
    {
        ItemStack blade = createIceBlade(player);
        player.getInventory().addItem(blade);
    }

    private ItemStack createIceBlade(Player player)
    {
        ItemStack blade = new ItemStack(Material.WOODEN_SWORD);

        ItemMeta meta = blade.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA +""+ ChatColor.BOLD +""+ getAbilityID());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC+ "Hurry, it's melting!");
        meta.setLore(lore);
        // Set custom attributes - damage
        //skill_meta.setCustomModelData(34);
        blade.setItemMeta(meta);

        ItemMetaTag.setTimeStampedItemMeta(plugin, skillKey, blade);

        return blade;
    }

    public ItemStack getAbilityItem(JavaPlugin plugin, Player player)
    {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "The perfect murder");
        lore.add(ChatColor.GRAY + "weapon");
        return getAbilityItem(plugin, player, lore, 1);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        int slot = e.getSlot();
        Inventory inv = e.getClickedInventory();

        if(inv == null) return;

        ItemStack item = inv.getItem(slot);
        if (item != null && ItemMetaTag.itemStackHasAnyTag(plugin, item, skillKey))
        {
            if(ItemMetaTag.getTimeElapsedEntityMeta(plugin, item, skillKey) > expirationTime)
            {
                for(HumanEntity entity : inv.getViewers())
                {
                    entity.sendMessage("Your ice blade melted!");
                }
                inv.clear(slot);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent e)
    {

        if (e.getDamager() instanceof Player player)
        {
            PlayerInventory inv = player.getInventory();
            ItemStack item = inv.getItemInMainHand();
            if (item != null && ItemMetaTag.itemStackHasAnyTag(plugin, item, skillKey))
            {
                if(ItemMetaTag.getTimeElapsedEntityMeta(plugin, item, skillKey) > expirationTime)
                {
                    player.sendMessage("Your ice blade melted!");
                    inv.setItemInMainHand(new ItemStack(Material.AIR));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        if (    e.getHand() == EquipmentSlot.HAND &&
                item != null &&
                ItemMetaTag.itemStackHasAnyTag(plugin, item, skillKey))
        {
            if(ItemMetaTag.getTimeElapsedEntityMeta(plugin, item, skillKey) > expirationTime)
            {
                player.sendMessage("Your ice blade melted!");
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                e.setCancelled(true);
            }
        }
    }

}

