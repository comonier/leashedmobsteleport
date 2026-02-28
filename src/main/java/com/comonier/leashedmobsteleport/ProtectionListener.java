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

    public ProtectionListener(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    // Impede que o mob pacificado pegue fogo (sol ou outras fontes)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSunBurn(EntityCombustEvent event) { 
        if (event.getEntity().getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
    
    // Impede qualquer dano ao mob enquanto estiver sob proteção do plugin
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageEvent event) { 
        if (event.getEntity().getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashBreak(EntityUnleashEvent event) {
        if (false == (event.getEntity() instanceof LivingEntity)) return;
        LivingEntity mob = (LivingEntity) event.getEntity();
        
        // Se o laço quebrar sozinho (distância/bug) e o mob for protegido
        if (mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
            if (event.getReason() != EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
                event.setDropLeash(false);
                // Limpa laços que droparem no chão por erro do motor do jogo
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Entity e : mob.getNearbyEntities(3, 3, 3)) {
                        if (e instanceof Item item) {
                            if (item.getItemStack().getType() == Material.LEAD) item.remove();
                        }
                    }
                }, 1L);
                return;
            }
        }

        // Se o jogador soltar o mob manualmente
        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setDropLeash(false); 
            plugin.getMobUtils().unpacify(mob);
            
            Player p = plugin.getMobUtils().findNearbyPlayer(mob);
            if (p != null) {
                p.sendMessage(plugin.getMessageManager().getMsg("unleash_success")
                    .replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                
                // Devolve o laço para o inventário ou dropa no pé se estiver cheio
                if (org.bukkit.GameMode.CREATIVE != p.getGameMode()) {
                    ItemStack lead = new ItemStack(Material.LEAD);
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItemNaturally(p.getLocation(), lead);
                    } else {
                        p.getInventory().addItem(lead);
                    }
                }
            }
        }
    }
}
