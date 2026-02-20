package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LeashedMobsTeleport extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration msgConfig;
    private final String PROTECT_KEY = "LMT_PROTECTED";
    private final String OWNER_KEY = "LMT_OWNER_UUID";
    private final Set<UUID> disabledMessages = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createMessagesConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("lmt") != null) getCommand("lmt").setExecutor(this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Entity e : player.getNearbyEntities(25, 25, 25)) {
                    if (e instanceof LivingEntity mob) {
                        boolean isOurs = mob.hasMetadata(OWNER_KEY) && mob.getMetadata(OWNER_KEY).get(0).asString().equals(player.getUniqueId().toString());
                        if ((mob.isLeashed() && player.equals(mob.getLeashHolder())) || isOurs) {
                            if (!mob.getWorld().equals(player.getWorld()) || mob.getLocation().distanceSquared(player.getLocation()) > 100.0) {
                                mob.teleport(player.getLocation());
                                mob.setLeashHolder(player);
                                pacify(mob);
                            }
                        }
                    }
                }
            }
        }, 2L, 2L);
        
        Bukkit.getScheduler().runTaskLater(this, this::printBanner, 1L);
    }

    private void printBanner() {
        Bukkit.getConsoleSender().sendMessage("§b ");
        Bukkit.getConsoleSender().sendMessage("§b##########################################");
        Bukkit.getConsoleSender().sendMessage("§b#     LEASHED MOBS TELEPORT - ACTIVE     #");
        Bukkit.getConsoleSender().sendMessage("§b#             VERSION: 1.6               #");
        Bukkit.getConsoleSender().sendMessage("§b##########################################");
        Bukkit.getConsoleSender().sendMessage("§b ");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            if (disabledMessages.contains(p.getUniqueId())) {
                disabledMessages.remove(p.getUniqueId());
                p.sendMessage(getMsg("toggle_on"));
            } else {
                disabledMessages.add(p.getUniqueId());
                p.sendMessage(getMsg("toggle_off"));
            }
            return true;
        }
        return false;
    }

    private String getEntityColorName(Entity e) {
        if (e instanceof Monster) return "§c" + e.getName() + "§7";
        return "§a" + e.getName() + "§7";
    }

    private String formatWorldName(String name) {
        name = name.toLowerCase();
        if (name.contains("nether")) return "§cNether§7"; 
        if (name.contains("end")) return "§dEnd§7"; 
        return "§aOverworld§7"; 
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
        if (mob instanceof Mob m) { m.setAware(false); m.setTarget(null); }
        if (mob instanceof Creeper c) { c.setIgnited(false); c.setMaxFuseTicks(99999); }
    }

    private void unpacify(LivingEntity mob) {
        mob.setInvulnerable(false);
        mob.setRemoveWhenFarAway(true);
        mob.removeMetadata(PROTECT_KEY, this);
        mob.removeMetadata(OWNER_KEY, this);
        if (mob instanceof Mob m) m.setAware(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity mob)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (item != null && item.getType() == Material.LEAD) {
            event.setCancelled(true);
            if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) return;
            if (mob.setLeashHolder(player)) {
                pacify(mob);
                mob.setMetadata(OWNER_KEY, new FixedMetadataValue(this, player.getUniqueId().toString()));
                player.sendMessage(getMsg("leash_success").replace("%entity%", getEntityColorName(mob)));
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Entity vehicle = player.getVehicle();
        
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity e : player.getNearbyEntities(30, 30, 30)) {
            if (e instanceof LivingEntity mob && mob.isLeashed() && player.equals(mob.getLeashHolder())) targets.add(mob);
        }

        if (targets.isEmpty() && vehicle == null) return;

        // ANTI-DUPE: Varredura de limpeza no local de origem
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (from.getWorld() != null) {
                for (Entity e : from.getWorld().getNearbyEntities(from, 8, 8, 8)) {
                    if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) {
                        if (item.getTicksLived() < 20) e.remove();
                    }
                }
            }
        }, 2L);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline() || to == null) return;

            if (vehicle != null && vehicle.isValid()) {
                vehicle.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline() && vehicle.isValid()) vehicle.addPassenger(player);
                }, 4L);
            }

            for (LivingEntity mob : targets) {
                if (mob.isValid()) {
                    mob.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (mob.isValid() && player.isOnline()) {
                            mob.setLeashHolder(player);
                            pacify(mob);
                        }
                    }, 6L);
                }
            }

            if (disabledMessages.contains(player.getUniqueId())) return;
            String ridingStr = vehicle != null ? getMsg("riding_suffix").replace("%mount%", "§f" + vehicle.getName() + "§7") : "";
            player.sendMessage(getMsg("teleport_msg")
                    .replace("%from%", formatWorldName(event.getFrom().getWorld().getName()))
                    .replace("%to%", formatWorldName(to.getWorld().getName()))
                    .replace("%count%", String.valueOf(targets.size()))
                    .replace("%riding%", ridingStr));
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSunBurn(EntityCombustEvent event) {
        if (event.getEntity().hasMetadata(PROTECT_KEY)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(PROTECT_KEY)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashBreak(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        
        if (mob.hasMetadata(PROTECT_KEY)) {
            // Correção técnica: usamos a enumeração completa para evitar erro de símbolo
            if (event.getReason() != EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
                event.setDropLeash(false);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    for (Entity e : mob.getNearbyEntities(3, 3, 3)) {
                        if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) e.remove();
                    }
                }, 1L);
                return;
            }
        }

        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setDropLeash(false);
            unpacify(mob);
            Player p = findNearbyPlayer(mob);
            if (p != null) {
                p.sendMessage(getMsg("unleash_success").replace("%entity%", getEntityColorName(mob)));
                if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.getInventory().addItem(new ItemStack(Material.LEAD));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFenceInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LeashHitch hitch) {
            Player p = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Entity nearby : hitch.getNearbyEntities(7, 7, 7)) {
                    if (nearby instanceof LivingEntity mob && mob.isLeashed()) {
                        String key = mob.getLeashHolder().equals(hitch) ? "fence_leash" : "fence_unleash";
                        pacify(mob);
                        p.sendMessage(getMsg(key).replace("%entity%", getEntityColorName(mob)));
                    }
                }
            }, 3L);
        }
    }

    private Player findNearbyPlayer(LivingEntity mob) {
        Player p = null;
        double dist = 900.0;
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
