package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LeashedMobsTeleport extends JavaPlugin implements Listener {

    private FileConfiguration msgConfig;
    private final String PROTECT_KEY = "LMT_PROTECTED";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createMessagesConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage("§a**** " + getName() + " v1.9 - ATIVADO ****");
    }

    private void createMessagesConfig() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) saveResource("messages.yml", false);
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    private String getMsg(String key) {
        String lang = getConfig().getString("language", "en");
        String raw = msgConfig.getString(lang + "." + key, "Missing: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    private void pacify(LivingEntity mob) {
        mob.setInvulnerable(true);
        mob.setFireTicks(0);
        mob.setRemoveWhenFarAway(false);
        mob.setMetadata(PROTECT_KEY, new FixedMetadataValue(this, true));
        if (mob instanceof Mob m) {
            m.setAware(false);
            m.setTarget(null);
        }
        if (mob instanceof Allay allay) allay.setGravity(true);
        if (mob instanceof Creeper c) {
            c.setIgnited(false);
            c.setMaxFuseTicks(99999);
        }
    }

    private void unpacify(LivingEntity mob) {
        mob.setInvulnerable(false);
        mob.setRemoveWhenFarAway(true);
        mob.removeMetadata(PROTECT_KEY, this);
        if (mob instanceof Mob m) m.setAware(true);
        if (mob instanceof Allay allay) allay.setGravity(false);
        if (mob instanceof Creeper c) c.setMaxFuseTicks(30);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity mob)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (item != null && item.getType() == Material.LEAD) {
            event.setCancelled(true);
            if (!player.hasPermission("leashedmobsteleport.use")) {
                player.sendMessage(getMsg("no_permission"));
                return;
            }
            if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
                player.sendMessage(getMsg("no_bosses"));
                return;
            }
            if (!mob.isLeashed() || !mob.getLeashHolder().equals(player)) {
                if (mob.setLeashHolder(player)) {
                    pacify(mob);
                    player.sendMessage(getMsg("leash_success").replace("%entity%", mob.getName()));
                    if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSunBurn(EntityCombustEvent event) {
        if (event.getEntity().hasMetadata(PROTECT_KEY)) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(PROTECT_KEY)) event.setCancelled(true);
    }

    /* 
     * TELEPORTE v1.9: TÉCNICA DE PASSAGEIRO INSTANTÂNEO
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        List<LivingEntity> targets = new ArrayList<>();
        
        for (Entity e : player.getNearbyEntities(32, 32, 32)) {
            if (e instanceof LivingEntity mob && mob.isLeashed() && player.equals(mob.getLeashHolder())) {
                targets.add(mob);
                // Força o teleporte junto com o player anexando como passageiro temporário
                player.addPassenger(mob); 
            }
        }

        if (targets.isEmpty() && !player.isInsideVehicle()) return;

        Entity vehicle = player.getVehicle();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) return;

            // Resolve a montaria principal
            if (vehicle instanceof LivingEntity v) {
                v.teleport(event.getTo());
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline() && v.isValid()) v.addPassenger(player);
                }, 5L);
            }

            // Resolve os mobs laçados que foram como passageiros
            for (LivingEntity mob : targets) {
                if (mob.isValid()) {
                    player.removePassenger(mob); // Desembarca no destino
                    mob.teleport(event.getTo()); // Garante posição exata
                    
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (player.isOnline() && mob.isValid()) {
                            mob.setLeashHolder(player);
                            pacify(mob);
                        }
                    }, 10L);
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            if (!(mob.getLeashHolder() instanceof LeashHitch)) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (mob.isValid() && !mob.isLeashed()) unpacify(mob);
                }, 100L);
                
                Player p = findNearbyPlayer(mob);
                if (p != null) {
                    p.sendMessage(getMsg("unleash_success").replace("%entity%", mob.getName()));
                    if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.getInventory().addItem(new ItemStack(Material.LEAD));
                }
                event.setDropLeash(false);
            }
        }
    }

    @EventHandler
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (event.getEntity().hasMetadata(PROTECT_KEY)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFenceInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LeashHitch hitch) {
            Player p = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Entity nearby : hitch.getNearbyEntities(7, 7, 7)) {
                    if (nearby instanceof LivingEntity mob && mob.isLeashed()) {
                        if (mob.getLeashHolder().equals(hitch)) {
                            pacify(mob);
                            p.sendMessage(getMsg("fence_leash").replace("%entity%", mob.getName()));
                        } else if (mob.getLeashHolder().equals(p)) {
                            pacify(mob);
                            p.sendMessage(getMsg("fence_unleash").replace("%entity%", mob.getName()));
                        }
                    }
                }
            }, 3L);
        }
    }

    private Player findNearbyPlayer(LivingEntity mob) {
        Player p = null;
        double dist = 2500.0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            double d = online.getLocation().distanceSquared(mob.getLocation());
            if (dist > d) { dist = d; p = online; }
        }
        return p;
    }

    @Override
    public void onDisable() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity mob && mob.hasMetadata(PROTECT_KEY)) unpacify(mob);
            }
        }
    }
}
