package com.comonier.leashedmobsteleport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;

public class WandSecurityListener implements Listener {
    private final LeashedMobsTeleport plugin;

    public WandSecurityListener(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    // Impede o jogador de dropar a Wand no chão (Tecla Q ou arrastando fora do inventário)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getItemManager().isWand(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    // Bloqueia mover a Wand para baús, funis, shulkers, ender chests, etc.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Se o inventário aberto NÃO for o do próprio jogador (ex: Baú, Fornalha, etc)
        if (false == (event.getInventory().getType() == InventoryType.CRAFTING || event.getInventory().getType() == InventoryType.PLAYER)) {
            
            // Bloqueia colocar o item que está no cursor dentro do baú
            if (plugin.getItemManager().isWand(cursor)) {
                event.setCancelled(true);
                return;
            }
            
            // Bloqueia Shift-Click da Wand para dentro do baú
            if (event.isShiftClick() && plugin.getItemManager().isWand(clicked)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Bloqueia qualquer clique direto se o item clicado for a Wand em um inventário externo
        if (false == (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER)) {
            if (plugin.getItemManager().isWand(clicked)) {
                event.setCancelled(true);
            }
        }
    }

    // Remove a Wand da lista de drops quando o jogador morre (ela simplesmente some para não cair no chão)
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (plugin.getItemManager().isWand(item)) {
                iterator.remove();
            }
        }
    }
}
