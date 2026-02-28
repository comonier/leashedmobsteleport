package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class MobTicker extends BukkitRunnable {
    private final LeashedMobsTeleport plugin;

    public MobTicker(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // 1. MANTER IA DESLIGADA (PDC/DNA)
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof LivingEntity) {
                    LivingEntity mob = (LivingEntity) e;
                    if (mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
                        if (mob instanceof Mob) {
                            Mob m = (Mob) mob;
                            if (m.isAware()) {
                                m.setAware(false);
                                m.setTarget(null);
                            }
                        }
                    }
                }
            }
        }

        // 2. TELEPORTE (APENAS PARA QUEM JÁ ESTÁ NA MÃO)
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Entity e : player.getNearbyEntities(25, 25, 25)) {
                if (false == (e instanceof LivingEntity)) continue;
                LivingEntity le = (LivingEntity) e;

                // SE NÃO TIVER LAÇO OU O LAÇO NÃO FOR O PLAYER, IGNORE TOTALMENTE
                if (false == le.isLeashed()) continue;
                if (false == player.equals(le.getLeashHolder())) continue;

                // Se o dono no DNA for o player atual
                String ownerUuidStr = le.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
                boolean isOurs = ownerUuidStr != null && ownerUuidStr.equals(player.getUniqueId().toString());
                
                if (isOurs) {
                    // Ignora se estiver sendo montado
                    if (false == le.getPassengers().isEmpty()) continue;

                    boolean mundoDif = false == le.getWorld().equals(player.getWorld());
                    double distSq = le.getLocation().distanceSquared(player.getLocation());

                    // Só teleporta se estiver realmente longe ou em outro mundo
                    if (mundoDif || distSq > 100.0) {
                        le.teleport(player.getLocation());
                        le.setLeashHolder(player);
                        plugin.getMobUtils().pacify(le);
                    }
                }
            }
        }
    }
}
