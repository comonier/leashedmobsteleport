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
        for (Entity entity : event.getEntities()) {
            if (entity instanceof LivingEntity) {
                LivingEntity mob = (LivingEntity) entity;
                if (mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
                    if (mob instanceof Mob) {
                        Mob m = (Mob) mob;
                        m.setAware(false);
                        m.setTarget(null);
                        mob.setRemoveWhenFarAway(false);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (false == (event.getRightClicked() instanceof LivingEntity)) return;
        
        LivingEntity mob = (LivingEntity) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        String ownerUuid = mob.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
        boolean isDono = (ownerUuid != null && ownerUuid.equals(player.getUniqueId().toString()));

        if (plugin.getItemManager().isWand(item)) {
            event.setCancelled(true);
            if (isDono) {
                if (mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch) {
                    LeashHitch hitch = (LeashHitch) mob.getLeashHolder();
                    hitch.remove();
                }
                mob.setLeashHolder(player);
                plugin.getMobUtils().pacify(mob);
                player.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                return;
            }
            if (false == mob.isLeashed()) {
                if (mob.setLeashHolder(player)) {
                    plugin.getMobUtils().pacify(mob);
                    mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
                    player.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                }
            }
            return;
        }

        if (Material.LEAD == item.getType()) {
            if (mob.isLeashed() && player.equals(mob.getLeashHolder())) {
                event.setCancelled(true);
                return;
            }
            if (ProtectionHooks.isProtected(player, mob.getLocation())) {
                event.setCancelled(true);
                return;
            }
            if (mob instanceof Monster || mob instanceof Villager || mob instanceof Slime) {
                event.setCancelled(true);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (mob.isValid() && player.isOnline() && mob.setLeashHolder(player)) {
                    plugin.getMobUtils().pacify(mob);
                    mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
                    player.sendMessage(plugin.getMessageManager().getMsg("leash_success").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                    if (org.bukkit.GameMode.CREATIVE != player.getGameMode()) {
                        ItemStack handItem = player.getInventory().getItemInMainHand();
                        if (handItem != null && Material.LEAD == handItem.getType()) {
                            handItem.setAmount(handItem.getAmount() - 1);
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onFenceInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || false == event.getClickedBlock().getType().name().contains("FENCE")) return;
        
        Player p = event.getPlayer();
        Location fenceLoc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
        ItemStack item = p.getInventory().getItemInMainHand();

        for (Entity nearby : p.getNearbyEntities(10, 10, 10)) {
            if (nearby instanceof LivingEntity) {
                LivingEntity mob = (LivingEntity) nearby;
                if (mob.hasMetadata(COOLDOWN_KEY)) continue;
                
                String ownerUuid = mob.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
                boolean isDono = (ownerUuid != null && ownerUuid.equals(p.getUniqueId().toString()));

                // PRENDER NA CERCA: Só prende se o mob estiver LAÇADO NA MÃO do player
                if (event.getAction().name().contains("RIGHT") && isDono) {
                    // TRAVA CRÍTICA: Verifica se o player é o Holder físico atual
                    if (mob.isLeashed() && p.equals(mob.getLeashHolder())) {
                        if (plugin.getItemManager().isWand(item) || Material.LEAD == item.getType()) {
                            event.setCancelled(true);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                LeashHitch hitch = mob.getWorld().spawn(fenceLoc, LeashHitch.class);
                                if (mob.setLeashHolder(hitch)) {
                                    plugin.getMobUtils().pacify(mob);
                                    p.sendMessage(plugin.getMessageManager().getMsg("fence_leash").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                                } else {
                                    hitch.remove();
                                }
                            }, 3L);
                            return;
                        }
                    }
                } 
                
                // SOLTAR DA CERCA: Só solta se estiver em um nó de cerca (LeashHitch)
                else if (isDono && mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch) {
                    LeashHitch hitch = (LeashHitch) mob.getLeashHolder();
                    double distSq = hitch.getLocation().distanceSquared(fenceLoc);
                    if (2.5 >= distSq) {
                        event.setCancelled(true);
                        int leadAntes = getLeadCount(p);

                        if (mob.setLeashHolder(p)) {
                            hitch.remove();
                            if (false == plugin.getItemManager().isWand(item)) {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    if (getLeadCount(p) == leadAntes && org.bukkit.GameMode.CREATIVE != p.getGameMode()) {
                                        if (-1 == p.getInventory().firstEmpty()) p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.LEAD));
                                        else p.getInventory().addItem(new ItemStack(Material.LEAD));
                                    }
                                }, 1L);
                            }
                            mob.setMetadata(COOLDOWN_KEY, new FixedMetadataValue(plugin, true));
                            Bukkit.getScheduler().runTaskLater(plugin, () -> mob.removeMetadata(COOLDOWN_KEY, plugin), 10L);
                            p.sendMessage(plugin.getMessageManager().getMsg("fence_unleash").replace("%entity%", plugin.getMobUtils().getEntityColorName(mob)));
                        }
                        return;
                    }
                }
            }
        }
    }

    private int getLeadCount(Player p) {
        int count = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && Material.LEAD == is.getType()) count += is.getAmount();
        }
        return count;
    }
}
