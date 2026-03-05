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
    public LeashListener(LeashedMobsTeleport plugin) { this.plugin = plugin; }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof LivingEntity mob && mob.getPersistentDataContainer().has(plugin.getProtectKey(), PersistentDataType.BYTE)) {
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
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ProtectionHooks.isProtected(player, mob.getLocation())) {
            event.setCancelled(true);
            if (mob.isLeashed() && player.equals(mob.getLeashHolder())) player.sendMessage("§c§lLMT §8» §cVocê não pode interagir ou soltar entidades aqui!");
            return;
        }
        String ownerUuid = mob.getPersistentDataContainer().get(plugin.getOwnerKey(), PersistentDataType.STRING);
        boolean isDono = (null != ownerUuid && ownerUuid.equals(player.getUniqueId().toString()));
        if (plugin.getItemManager().isWand(item)) {
            event.setCancelled(true);
            mob.getPersistentDataContainer().set(plugin.getWandLeashKey(), PersistentDataType.BYTE, (byte)1);
            if (isDono) {
                if (mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch hitch) hitch.remove();
                mob.setLeashHolder(player);
                plugin.getMobUtils().pacify(mob);
                return;
            }
            if (false == mob.isLeashed() && mob.setLeashHolder(player)) {
                plugin.getMobUtils().pacify(mob);
                mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            }
            return;
        }
        if (Material.LEAD == item.getType()) {
            if (mob.isLeashed() && player.equals(mob.getLeashHolder())) { event.setCancelled(true); return; }
            mob.getPersistentDataContainer().remove(plugin.getWandLeashKey());
            if (mob instanceof Monster || mob instanceof Villager || mob instanceof Slime) event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (mob.isValid() && player.isOnline() && mob.setLeashHolder(player)) {
                    plugin.getMobUtils().pacify(mob);
                    mob.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
                    if (org.bukkit.GameMode.CREATIVE != player.getGameMode()) {
                        ItemStack hand = player.getInventory().getItemInMainHand();
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
        if (ProtectionHooks.isProtected(p, fenceLoc)) {
            if (p.getInventory().getItemInMainHand().getType() == Material.LEAD || plugin.getItemManager().isWand(p.getInventory().getItemInMainHand())) {
                p.sendMessage("§c§lLMT §8» §cVocê não tem permissão para usar cercas neste terreno!");
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
                            else plugin.getMobUtils().pacify(mob);
                        }, 3L);
                    }
                } else if (isDono && mob.isLeashed() && mob.getLeashHolder() instanceof LeashHitch hitch) {
                    if (2.5 >= hitch.getLocation().distanceSquared(fenceLoc)) {
                        event.setCancelled(true);
                        if (mob.setLeashHolder(p)) {
                            hitch.remove();
                            mob.setMetadata(COOLDOWN_KEY, new FixedMetadataValue(plugin, true));
                            Bukkit.getScheduler().runTaskLater(plugin, () -> mob.removeMetadata(COOLDOWN_KEY, plugin), 10L);
                        }
                    }
                }
            }
        }
    }
}
