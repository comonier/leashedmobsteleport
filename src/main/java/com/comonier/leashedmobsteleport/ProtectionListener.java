package com.comonier.leashedmobsteleport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
public class ProtectionListener implements Listener {
    private final LeashedMobsTeleport plugin;
    public ProtectionListener(LeashedMobsTeleport plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSunBurn(EntityCombustEvent event) { 
        if (event.getEntity().getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageEvent event) { 
        if (event.getEntity().getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashBreak(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        boolean isWand = mob.getPersistentDataContainer().has(plugin.getWandLeashKey(), PersistentDataType.BYTE);
        if (mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
            if (event.getReason() != EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
                event.setDropLeash(false);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Entity e : mob.getNearbyEntities(3, 3, 3)) if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) item.remove();
                }, 1L);
                return;
            }
        }
        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setDropLeash(false);
            plugin.getMobUtils().unpacify(mob);
            Player p = plugin.getMobUtils().findNearbyPlayer(mob);
            if (null != p) {
                p.sendMessage(plugin.getMessageManager().getMsg("unleash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                if (org.bukkit.GameMode.CREATIVE != p.getGameMode() && false == isWand) {
                    ItemStack lead = new ItemStack(Material.LEAD);
                    if (p.getInventory().firstEmpty() == -1) p.getWorld().dropItemNaturally(p.getLocation(), lead);
                    else p.getInventory().addItem(lead);
                }
            }
            mob.getPersistentDataContainer().remove(plugin.getWandLeashKey());
        }
    }
}
