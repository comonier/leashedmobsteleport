package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;

public class MobUtils {
    private final LeashedMobsTeleport plugin;

    public MobUtils(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    public void pacify(LivingEntity mob) {
        mob.setInvulnerable(true);
        mob.setFireTicks(0);
        mob.setRemoveWhenFarAway(false);
        // GRAVAÇÃO PERSISTENTE (DNA)
        mob.getPersistentDataContainer().set(plugin.getProtectKey(), PersistentDataType.BYTE, (byte) 1);
        
        if (mob instanceof Mob m) {
            m.setAware(false);
            m.setTarget(null);
        }
        if (mob instanceof Creeper c) {
            c.setIgnited(false);
            c.setMaxFuseTicks(99999);
        }
    }

    public void unpacify(LivingEntity mob) {
        mob.setInvulnerable(false);
        mob.setRemoveWhenFarAway(true);
        // REMOVE DO DNA
        mob.getPersistentDataContainer().remove(plugin.getProtectKey());
        mob.getPersistentDataContainer().remove(plugin.getOwnerKey());
        
        if (mob instanceof Mob m) m.setAware(true);
        if (mob instanceof Creeper c) c.setMaxFuseTicks(30);
    }

    public void unpacifyAll() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity mob && mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
                    unpacify(mob);
                }
            }
        }
    }

    public String getEntityColorName(Entity e) {
        return (e instanceof Monster) ? "§c" + e.getName() + "§7" : "§a" + e.getName() + "§7";
    }

    public String formatWorldName(String name) {
        name = name.toLowerCase();
        if (name.contains("nether")) return "§cNether§7"; 
        if (name.contains("end")) return "§dEnd§7"; 
        return "§aOverworld§7"; 
    }

    public Player findNearbyPlayer(LivingEntity mob) {
        Player p = null; 
        double dist = 900.0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (false == online.getWorld().equals(mob.getWorld())) continue;
            double d = online.getLocation().distanceSquared(mob.getLocation());
            if (dist > d) { dist = d; p = online; }
        }
        return p;
    }
}
