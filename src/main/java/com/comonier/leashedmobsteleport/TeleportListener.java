package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;

public class TeleportListener implements Listener {
    private final LeashedMobsTeleport plugin;

    public TeleportListener(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Entity vehicle = player.getVehicle();
        
        // Se for no mesmo mundo e distância curta, ignora
        boolean mesmoMundo = from.getWorld().equals(to.getWorld());
        if (mesmoMundo && 25.0 >= from.distanceSquared(to)) return;
        
        List<LivingEntity> targets = new ArrayList<>();
        
        // --- TRAVA DE SEGURANÇA ---
        // Procuramos mobs APENAS no local de ORIGEM (From). 
        // Nunca no destino (To), para não puxar mobs que já estão em casa.
        for (Entity e : from.getWorld().getNearbyEntities(from, 30, 30, 30)) {
            if (e instanceof LivingEntity m) {
                // SÓ adiciona se o player for o Holder físico AGORA (antes de viajar)
                if (m.isLeashed() && player.equals(m.getLeashHolder())) {
                    // E se o mob não estiver em uma cerca
                    if (false == (m.getLeashHolder() instanceof LeashHitch)) {
                        targets.add(m);
                    }
                }
            }
        }
        
        if (targets.isEmpty() && vehicle == null) return;
        
        // Limpeza Anti-Dupe no local de ORIGEM
        for (int i = 1; 4 > i; i++) {
            final int wave = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (from.getWorld() != null) {
                    for (Entity e : from.getWorld().getNearbyEntities(from, 10, 10, 10)) {
                        if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) item.remove();
                    }
                }
            }, wave * 2L);
        }

        // Teleporta quem estava COM VOCÊ na origem para o destino
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (false == player.isOnline() || to == null) return;
            
            if (vehicle != null && vehicle.isValid()) {
                vehicle.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && vehicle.isValid()) vehicle.addPassenger(player);
                }, 15L); 
            }

            for (LivingEntity m : targets) {
                if (m.isValid()) {
                    m.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (m.isValid() && player.isOnline()) {
                            m.setLeashHolder(player);
                            plugin.getMobUtils().pacify(m);
                        }
                    }, 6L);
                }
            }
        }, 2L);
    }
}
