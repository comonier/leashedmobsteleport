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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Claim;

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
        reloadPluginData();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("lmt") != null) getCommand("lmt").setExecutor(this);

        // TASK DE TELEPORTE CONTÍNUO - v1.5.5 (PROTEÇÃO DE CERCA E PASSAGEIRO)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Entity e : player.getNearbyEntities(25, 25, 25)) {
                    
                    // SE TIVER PASSAGEIRO (DOMANDO/MONTADO) OU PRESO NA CERCA (LEASH HITCH), NÃO TELEPORTA
                    if (!e.getPassengers().isEmpty()) continue;
                    if (e instanceof LivingEntity le && le.isLeashed() && le.getLeashHolder() instanceof LeashHitch) continue;

                    if (e instanceof LivingEntity mob) {
                        boolean isOurs = mob.hasMetadata(OWNER_KEY) && !mob.getMetadata(OWNER_KEY).isEmpty() && 
                                         mob.getMetadata(OWNER_KEY).get(0).asString().equals(player.getUniqueId().toString());
                        
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
        }, 10L, 10L);
        
        Bukkit.getScheduler().runTaskLater(this, this::printBanner, 1L);
    }

    private void reloadPluginData() {
        saveDefaultConfig();
        reloadConfig();
        createMessagesConfig();
    }

    private void printBanner() {
        Bukkit.getConsoleSender().sendMessage("§b ");
        Bukkit.getConsoleSender().sendMessage("§b##########################################");
        Bukkit.getConsoleSender().sendMessage("§b#     LEASHED MOBS TELEPORT - v1.5.5     #");
        Bukkit.getConsoleSender().sendMessage("§b#    ESTÁVEL: WG / GP / ANTI-DUPE LEAD   #");
        Bukkit.getConsoleSender().sendMessage("§b##########################################");
        Bukkit.getConsoleSender().sendMessage("§b ");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("leashedmobsteleport.admin")) {
                    sender.sendMessage(getMsg("no_permission_admin"));
                    return true;
                }
                reloadPluginData();
                sender.sendMessage(getMsg("reload_success"));
                return true;
            }
            if (args[0].equalsIgnoreCase("toggle")) {
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
            }
        }
        String status = getConfig().getBoolean("use-permission-per-mob", false) ? getMsg("enabled") : getMsg("disabled");
        sender.sendMessage(getMsg("info_header"));
        sender.sendMessage(getMsg("info_version").replace("%version%", getDescription().getVersion()));
        sender.sendMessage(getMsg("info_perm_status").replace("%status%", status));
        sender.sendMessage(getMsg("info_commands"));
        sender.sendMessage("§fDownload: §bspigotmc.org/resources/leashedmobsteleport.119429/");
        sender.sendMessage("§fSupport: §://bgithub.com");
        return true;
    }

    private boolean isProtected(Player p, Location l) {
        if (p.isOp() || p.hasPermission("leashedmobsteleport.admin")) return false;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            if (!query.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.INTERACT) ||
                !query.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.USE)) return true; 
        }
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(l, false, null);
            if (claim != null && claim.allowAccess(p) != null) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof LivingEntity mob)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.LEAD) {
            if (isProtected(player, mob.getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (getConfig().getBoolean("use-permission-per-mob", false)) {
                String perMobPerm = "lmt.leash." + mob.getType().name().toLowerCase();
                if (!player.hasPermission(perMobPerm)) {
                    event.setCancelled(true);
                    player.sendMessage(getMsg("no_permission_mob").replace("%entity%", mob.getName()).replace("%permission%", perMobPerm));
                    return;
                }
            } else if (!player.hasPermission("leashedmobsteleport.use")) {
                event.setCancelled(true);
                player.sendMessage(getMsg("no_permission"));
                return;
            }

            if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
                player.sendMessage(getMsg("no_bosses"));
                event.setCancelled(true);
                return;
            }

            if (mob instanceof Monster || mob instanceof Slime || mob instanceof Ghast) {
                event.setCancelled(true);
            }

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (mob.isValid() && player.isOnline()) {
                    if (mob.setLeashHolder(player)) {
                        pacify(mob);
                        mob.setMetadata(OWNER_KEY, new FixedMetadataValue(this, player.getUniqueId().toString()));
                        player.sendMessage(getMsg("leash_success").replace("%entity%", getEntityColorName(mob)));
                        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                            ItemStack handItem = player.getInventory().getItemInMainHand();
                            if (handItem != null && handItem.getType() == Material.LEAD) handItem.setAmount(handItem.getAmount() - 1);
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        Entity vehicle = player.getVehicle();
        
        if (from.getWorld().equals(to.getWorld()) && from.distanceSquared(to) < 25.0) return;

        List<LivingEntity> targets = new ArrayList<>();
        for (Entity e : player.getNearbyEntities(30, 30, 30)) {
            if (e instanceof LivingEntity m && m.isLeashed() && player.equals(m.getLeashHolder())) targets.add(m);
        }

        if (targets.isEmpty() && vehicle == null) return;

        // ANTI-DUPE LEAD - LIMPEZA REFORÇADA (TICKS 2, 4 e 6)
        for (int i = 1; i <= 3; i++) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (from.getWorld() != null) {
                    for (Entity e : from.getWorld().getNearbyEntities(from, 10, 10, 10)) {
                        if (e instanceof Item item && item.getItemStack().getType() == Material.LEAD) item.remove();
                    }
                }
            }, i * 2L);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline() || to == null) return;

            if (vehicle != null && vehicle.isValid()) {
                vehicle.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline() && vehicle.isValid()) vehicle.addPassenger(player);
                }, 15L); 
            }

            for (LivingEntity m : targets) {
                if (m.isValid()) {
                    m.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (m.isValid() && player.isOnline()) {
                            m.setLeashHolder(player);
                            pacify(m);
                        }
                    }, 6L);
                }
            }

            if (!disabledMessages.contains(player.getUniqueId())) {
                String ridingStr = vehicle != null ? getMsg("riding_suffix").replace("%mount%", "§f" + vehicle.getName() + "§7") : "";
                player.sendMessage(getMsg("teleport_msg")
                    .replace("%from%", formatWorldName(from.getWorld().getName()))
                    .replace("%to%", formatWorldName(to.getWorld().getName()))
                    .replace("%count%", String.valueOf(targets.size()))
                    .replace("%riding%", ridingStr));
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFenceInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType().name().contains("FENCE")) {
                Player p = event.getPlayer();
                Location fenceLoc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
                
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    for (Entity nearby : p.getNearbyEntities(10, 10, 10)) {
                        if (nearby instanceof LivingEntity mob && mob.isLeashed() && p.equals(mob.getLeashHolder())) {
                            
                            // FORÇA O LAÇO NA CERCA PARA MONSTROS
                            if (mob instanceof Monster || mob instanceof Slime || mob instanceof Ghast) {
                                LeashHitch hitch = mob.getWorld().spawn(fenceLoc, LeashHitch.class);
                                mob.setLeashHolder(hitch);
                            }
                            
                            if (mob.getLeashHolder() instanceof LeashHitch) {
                                pacify(mob);
                                p.sendMessage(getMsg("fence_leash").replace("%entity%", getEntityColorName(mob)));
                            }
                        }
                    }
                }, 3L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSunBurn(EntityCombustEvent e) { if (e.getEntity().hasMetadata(PROTECT_KEY)) e.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDamage(EntityDamageEvent e) { if (e.getEntity().hasMetadata(PROTECT_KEY)) e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeashBreak(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        
        if (mob.hasMetadata(PROTECT_KEY) && event.getReason() != EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setDropLeash(false);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Entity e : mob.getNearbyEntities(3, 3, 3)) if (e instanceof Item i && i.getItemStack().getType() == Material.LEAD) e.remove();
            }, 1L);
            return;
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
        mob.setInvulnerable(true); mob.setFireTicks(0); mob.setRemoveWhenFarAway(false);
        mob.setMetadata(PROTECT_KEY, new FixedMetadataValue(this, true));
        if (mob instanceof Mob m) { m.setAware(false); m.setTarget(null); }
        if (mob instanceof Creeper c) { c.setIgnited(false); c.setMaxFuseTicks(99999); }
    }

    private void unpacify(LivingEntity mob) {
        mob.setInvulnerable(false); mob.setRemoveWhenFarAway(true);
        mob.removeMetadata(PROTECT_KEY, this); mob.removeMetadata(OWNER_KEY, this);
        if (mob instanceof Mob m) m.setAware(true);
    }

    private String getEntityColorName(Entity e) {
        return (e instanceof Monster) ? "§c" + e.getName() + "§7" : "§a" + e.getName() + "§7";
    }

    private String formatWorldName(String name) {
        name = name.toLowerCase();
        if (name.contains("nether")) return "§cNether§7"; 
        if (name.contains("end")) return "§dEnd§7"; 
        return "§aOverworld§7"; 
    }

    private Player findNearbyPlayer(LivingEntity mob) {
        Player p = null; double dist = 900.0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            double d = online.getLocation().distanceSquared(mob.getLocation());
            if (dist > d) { dist = d; p = online; }
        }
        return p;
    }

    @Override
    public void onDisable() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) if (entity instanceof LivingEntity mob && mob.hasMetadata(PROTECT_KEY)) unpacify(mob);
        }
    }
}
