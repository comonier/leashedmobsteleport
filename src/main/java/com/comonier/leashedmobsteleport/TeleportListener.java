package com.comonier.leashedmobsteleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.ArrayList;
import java.util.List;
public class TeleportListener implements Listener {
    private final LeashedMobsTeleport plugin;
    public TeleportListener(LeashedMobsTeleport plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Entity vehicle = player.getVehicle();
        boolean mesmoMundo = from.getWorld().equals(to.getWorld());
        if (mesmoMundo && 25.0 >= from.distanceSquared(to)) return;
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity e : from.getWorld().getNearbyEntities(from, 30, 30, 30)) {
            if (e instanceof LivingEntity m) {
                if (m.isLeashed() && player.equals(m.getLeashHolder())) {
                    if (false == (m.getLeashHolder() instanceof LeashHitch)) targets.add(m);
                }
            }
        }
        if (targets.isEmpty() && null == vehicle) return;
        for (int i = 1; 4 > i; i++) {
            final int wave = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (null != from.getWorld()) {
                    for (Entity e : from.getWorld().getNearbyEntities(from, 10, 10, 10)) {
                        if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) item.remove();
                    }
                }
            }, wave * 2L);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (false == player.isOnline() || null == to) return;
            if (null != vehicle && vehicle.isValid()) {
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
