package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class LeashedMobsTeleport extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        // Mensagem de inicialização estilizada e colorida conforme solicitado
        Bukkit.getConsoleSender().sendMessage("§a****");
        Bukkit.getConsoleSender().sendMessage("§a" + getName() + " §ev" + getDescription().getVersion() + " §a- ATIVADO");
        Bukkit.getConsoleSender().sendMessage("§aDesenvolvido por: §6comonier");
        Bukkit.getConsoleSender().sendMessage("§a****");
    }

    /* 
     * PARTE 1: SISTEMA DE LAÇO UNIVERSAL E PROTEÇÕES
     * Força o laço em qualquer mob, impedindo interações indesejadas (dupe, menus, montaria).
     * Mobs Adicionados/Liberados:
     * - Villagers (Impedindo abertura de trocas se estiver com laço)
     * - Allays (Impedindo coleta de itens/dupe de laço)
     * - Iron Golens e Snow Golens (Suporte total a golens)
     * - Mobs Hostis (Zombies, Skeletons, Creepers, etc)
     * - Nautilus (Montaria 1.21.11 - Impedindo montagem acidental)
     * - Breeze e Bogged (Novos mobs da 1.21)
     * - Cavalos Zumbi e Esqueleto (Montarias especiais)
     * - Reset de Creeper: Remove o alvo de ataque e atrasa o fusível para 99999 ticks.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity mob)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (item != null && item.getType() == Material.LEAD) {
            // Prioridade Total: Cancela a função padrão do mob (troca, montaria, etc)
            event.setCancelled(true); 

            if (!player.hasPermission("leashedmobsteleport.use")) return;

            // Bloqueio de Bosses: Wither e Ender Dragon não podem ser laçados
            if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
                player.sendMessage("§cVocê não pode laçar chefões!");
                return;
            }

            // Força o laço no mob e anula o alvo para ele parar de atacar
            if (!mob.isLeashed() || !mob.getLeashHolder().equals(player)) {
                if (mob.setLeashHolder(player)) {
                    // Remove o alvo de ataque se o mob for uma criatura capaz de atacar
                    if (mob instanceof Mob creature) {
                        creature.setTarget(null);
                    }
                    
                    // Reset de Creeper: Desativa ignição e coloca o fusível em tempo infinito
                    if (mob instanceof Creeper creeper) {
                        creeper.setIgnited(false);
                        creeper.setMaxFuseTicks(99999);
                    }

                    if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                        item.setAmount(item.getAmount() - 1);
                    }
                }
            }
        }
    }

    /* 
     * PARTE 2: LAÇO INQUEBRÁVEL POR DISTÂNCIA E RESET DE STATUS
     * Impede que o laço se quebre se o jogador voar ou se afastar rápido demais.
     * Devolve o tempo de explosão padrão ao Creeper quando solto.
     */
    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (event.getEntity() instanceof LivingEntity mob) {
            // Reseta o fusível do Creeper para o valor padrão (30 ticks) ao ser solto
            if (mob instanceof Creeper creeper) {
                creeper.setMaxFuseTicks(30);
            }
        }

        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            if (event.getEntity() instanceof LivingEntity mob && mob.isLeashed()) {
                if (mob.getLeashHolder() instanceof Player) {
                    event.setCancelled(true); // Cancela o rompimento por distância
                }
            }
        }
    }

    /* 
     * PARTE 3: TELEPORTE INTERDIMENSIONAL (Overworld, Nether, End)
     * Captura os mobs antes do 'chunk unload' e reconecta o laço no destino após 1 tick.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leashedmobsteleport.use")) return;

        List<LivingEntity> leashedMobs = player.getNearbyEntities(25, 25, 25).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .map(entity -> (LivingEntity) entity)
            .filter(leashed -> leashed.isLeashed() && leashed.getLeashHolder().equals(player))
            .collect(Collectors.toList());

        for (LivingEntity mob : leashedMobs) {
            mob.teleport(event.getTo());
            Bukkit.getScheduler().runTask(this, () -> {
                if (mob.isValid() && player.isOnline()) {
                    mob.setLeashHolder(player);
                    // Garante que continue pacificado no destino
                    if (mob instanceof Mob creature) {
                        creature.setTarget(null);
                    }
                }
            });
        }
    }

    /* 
     * PARTE 4: EVENTO NATIVO DE LAÇO
     * Garante que o laço padrão do jogo também pacifique o mob e resete Creepers.
     */
    @EventHandler
    public void onLeash(PlayerLeashEntityEvent event) {
        if (!event.getPlayer().hasPermission("leashedmobsteleport.use")) {
            event.setCancelled(true);
        } else {
            if (event.getEntity() instanceof LivingEntity mob) {
                if (mob instanceof Mob creature) {
                    creature.setTarget(null);
                }
                if (mob instanceof Creeper creeper) {
                    creeper.setIgnited(false);
                    creeper.setMaxFuseTicks(99999);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // SEGURANÇA: Reseta os fusíveis dos Creepers laçados ao desligar o plugin
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof Creeper creeper && creeper.isLeashed()) {
                    creeper.setMaxFuseTicks(30);
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage("§a[LeashedMobsTeleport] Plugin desativado e status resetados.");
    }
}
