package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

public class LeashListener implements Listener {
    private final LeashedMobsTeleport plugin;
    private final String COOLDOWN_KEY = "LMT_COOLDOWN";

    public LeashListener(LeashedMobsTeleport plugin) { 
        this.plugin = plugin; 
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity e : event.getEntities()) {
            if (e instanceof LivingEntity mob && mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
                if (mob instanceof Mob m) {
                    m.setAware(false);
                    m.setTarget(null);
                    mob.setRemoveWhenFarAway(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND || !(event.getRightClicked() instanceof LivingEntity mob)) return;
        
        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        // O ProtectionHooks já é seguro (blindado), então podemos chamar sem medo
        if (ProtectionHooks.isProtected(p, mob.getLocation())) {
            event.setCancelled(true);
            if (mob.isLeashed() && p.equals(mob.getLeashHolder())) {
                p.sendMessage(plugin.getMessageManager().getMsg("protection_no_access"));
            }
            return;
        }

        String ownerUuid = mob.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
        boolean isDono = (null != ownerUuid && ownerUuid.equals(p.getUniqueId().toString()));

        if (plugin.getItemManager().isWand(item)) {
            event.setCancelled(true);
            mob.getPersistentDataContainer().set(plugin.getWandLeashKey(), PersistentDataType.BYTE, (byte) 1);
            
            if (isDono) {
                if (mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch hitch) hitch.remove();
                mob.setLeashHolder(p);
                plugin.getMobUtils().pacify(mob);
                p.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                return;
            }

            if (false == mob.isLeashed() && mob.setLeashHolder(p)) {
                plugin.getMobUtils().pacify(mob);
                mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, p.getUniqueId().toString());
                p.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
            }
            return;
        }

        if (Material.LEAD == item.getType()) {
            if (mob.isLeashed() && p.equals(mob.getLeashHolder())) { 
                event.setCancelled(true); 
                return; 
            }
            
            mob.getPersistentDataContainer().remove(plugin.getWandLeashKey());
            
            // Permite laçar mobs que o Vanilla não deixa (Monstros/Villagers)
            if (mob instanceof Monster || mob instanceof Villager || mob instanceof Slime) event.setCancelled(true);
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (mob.isValid() && p.isOnline() && mob.setLeashHolder(p)) {
                    plugin.getMobUtils().pacify(mob);
                    mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, p.getUniqueId().toString());
                    p.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                    
                    if (org.bukkit.GameMode.CREATIVE != p.getGameMode()) {
                        ItemStack hand = p.getInventory().getItemInMainHand();
                        if (null != hand && Material.LEAD == hand.getType()) hand.setAmount(hand.getAmount() - 1);
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onFenceInteract(PlayerInteractEvent event) {
        if (null == event.getClickedBlock() || !event.getClickedBlock().getType().name().contains("FENCE")) return;
        
        Player p = event.getPlayer();
        Location fenceLoc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);

        // Uso do Hook Blindado
        if (ProtectionHooks.isProtected(p, fenceLoc)) {
            ItemStack inHand = p.getInventory().getItemInMainHand();
            if (inHand.getType() == Material.LEAD || plugin.getItemManager().isWand(inHand)) {
                p.sendMessage(plugin.getMessageManager().getMsg("protection_no_fence"));
                event.setCancelled(true);
            }
            return;
        }

        for (Entity nearby : p.getNearbyEntities(10, 10, 10)) {
            if (nearby instanceof LivingEntity mob) {
                if (mob.hasMetadata(COOLDOWN_KEY)) continue;
                
                String ownerUuid = mob.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
                boolean isDono = (null != ownerUuid && ownerUuid.equals(p.getUniqueId().toString()));

                if (event.getAction().name().contains("RIGHT") && isDono) {
                    if (mob.isLeashed() && p.equals(mob.getLeashHolder())) {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            LeashHitch hitch = mob.getWorld().spawn(fenceLoc, LeashHitch.class);
                            if (false == mob.setLeashHolder(hitch)) hitch.remove();
                            else {
                                plugin.getMobUtils().pacify(mob);
                                p.sendMessage(plugin.getMessageManager().getMsg("fence_leash").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                            }
                        }, 3L);
                    }
                } else if (isDono && mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch hitch) {
                    // Verifica se o mob está preso NESTA cerca específica
                    if (2.5 >= hitch.getLocation().distanceSquared(fenceLoc)) {
                        event.setCancelled(true);
                        if (mob.setLeashHolder(p)) {
                            hitch.remove();
                            mob.setMetadata(COOLDOWN_KEY, new FixedMetadataValue(plugin, true));
                            Bukkit.getScheduler().runTaskLater(plugin, () -> mob.removeMetadata(COOLDOWN_KEY, plugin), 10L);
                            p.sendMessage(plugin.getMessageManager().getMsg("fence_unleash").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                        }
                    }
                }
            }
        }
    }
}
