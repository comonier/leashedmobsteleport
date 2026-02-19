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
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LeashedMobsTeleport extends JavaPlugin implements Listener {

    private FileConfiguration msgConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createMessagesConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        // Mensagem de inicialização estilizada no console
        Bukkit.getConsoleSender().sendMessage("§a****");
        Bukkit.getConsoleSender().sendMessage("§a" + getName() + " §ev" + getDescription().getVersion() + " §a- ATIVADO");
        Bukkit.getConsoleSender().sendMessage("§aDesenvolvido por: §6comonier");
        Bukkit.getConsoleSender().sendMessage("§a****");
    }

    private void createMessagesConfig() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false);
        }
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    private String getMsg(String key) {
        String lang = getConfig().getString("language", "en");
        String raw = msgConfig.getString(lang + "." + key, "Missing: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /* 
     * PARTE 1: BLOQUEIO DE INTERAÇÃO E LAÇO UNIVERSAL
     * - Desliga a função do botão direito (event.setCancelled) para evitar abrir menus de Villagers,
     *   entregar itens para Allays ou montar acidentalmente em Cavalos e Nautilus (1.21.11).
     * - Suporte total para Iron Golens, Snow Golens, Breeze e mobs hostis.
     * - O laço remove o alvo (Target) do mob imediatamente.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCustomLeash(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity mob)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (item != null && item.getType() == Material.LEAD) {
            // Bloqueia interações acidentais (trocas, montaria, inventário do mob)
            event.setCancelled(true);

            if (!player.hasPermission("leashedmobsteleport.use")) {
                player.sendMessage(getMsg("no_permission"));
                return;
            }

            // Impede laçar chefões (Wither e Ender Dragon)
            if (mob.getType() == EntityType.WITHER || mob.getType() == EntityType.ENDER_DRAGON) {
                player.sendMessage(getMsg("no_bosses"));
                return;
            }

            if (!mob.isLeashed() || !mob.getLeashHolder().equals(player)) {
                if (mob.setLeashHolder(player)) {
                    // Desativa a agressividade do mob (Golens e Hostis)
                    if (mob instanceof Mob m) m.setTarget(null);
                    
                    // PARTE 2: DESATIVAÇÃO DO CREEPER
                    // Reseta o fusível e impede a explosão enquanto estiver laçado
                    if (mob instanceof Creeper creeper) {
                        creeper.setIgnited(false);
                        creeper.setMaxFuseTicks(99999);
                    }
                    
                    player.sendMessage(getMsg("leash_success").replace("%entity%", mob.getName()));

                    if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                        item.setAmount(item.getAmount() - 1);
                    }
                }
            }
        }
    }

    /* 
     * PARTE 3: GESTÃO DE CERCAS E RETORNO DO ITEM AO INVENTÁRIO
     * - Identifica se o mob foi solto no chão ou apenas retirado da cerca.
     * - Se solto no chão, a corda volta para o inventário (não dropa no chão).
     * - A IA só é reativada se o mob for solto (não apenas mudado de dono).
     */
    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        
        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            
            // Só reativa IA (incluindo Creepers) se NÃO estiver indo para uma cerca (libertação real)
            if (!(mob.getLeashHolder() instanceof LeashHitch)) {
                if (mob instanceof Creeper creeper) creeper.setMaxFuseTicks(30);
                
                Player p = findNearbyPlayer(mob);
                if (p != null) {
                    p.sendMessage(getMsg("unleash_success").replace("%entity%", mob.getName()));
                    // Devolve o laço ao inventário em vez de dropar no chão
                    if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                        p.getInventory().addItem(new ItemStack(Material.LEAD));
                    }
                }
                event.setDropLeash(false); // Cancela o drop físico no chão
            }
        }
    }

    /* 
     * PARTE 4: SISTEMA DE AVISOS DE CERCA (PRENDER E RETIRAR)
     * - Detecta quando o jogador prende o mob em uma cerca ou retira para a mão.
     * - Garante que a IA permaneça desligada em ambos os casos.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFenceInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LeashHitch hitch) {
            Player p = event.getPlayer();

            // Delay de 1 tick para verificar quem assumiu o laço (Cerca ou Player)
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Entity nearby : hitch.getNearbyEntities(7, 7, 7)) {
                    if (nearby instanceof LivingEntity mob && mob.isLeashed()) {
                        // Caso: Prendeu o mob que estava na mão na cerca
                        if (mob.getLeashHolder().equals(hitch)) {
                            p.sendMessage(getMsg("fence_leash").replace("%entity%", mob.getName()));
                        } 
                        // Caso: Retirou o mob da cerca para a mão (IA continua desligada)
                        else if (mob.getLeashHolder().equals(p)) {
                            p.sendMessage(getMsg("fence_unleash").replace("%entity%", mob.getName()));
                        }
                    }
                }
            }, 1L);
        }
    }

    /* 
     * PARTE 5: TELEPORTE INTERDIMENSIONAL E LAÇO INQUEBRÁVEL
     * - Garante que o laço não quebre por distância (voo rápido ou velocidade).
     * - Força os mobs laçados a atravessarem portais do Nether/End junto com o jogador.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        List<LivingEntity> leashedMobs = player.getNearbyEntities(30, 30, 30).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .map(entity -> (LivingEntity) entity)
            .filter(leashed -> leashed.isLeashed() && leashed.getLeashHolder().equals(player))
            .collect(Collectors.toList());

        if (leashedMobs.isEmpty()) return;

        for (final LivingEntity mob : leashedMobs) {
            // Teleporta o mob para a localização de destino do jogador (mesmo entre dimensões)
            if (mob.teleport(event.getTo())) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (mob.isValid() && player.isOnline()) {
                        mob.setLeashHolder(player); // Reforça o vínculo após o teleporte
                    }
                }, 2L);
            }
        }
        player.sendMessage(getMsg("teleport_success")
            .replace("%from%", event.getFrom().getWorld().getName())
            .replace("%to%", event.getTo().getWorld().getName()));
    }

    // Função auxiliar para encontrar o dono da corda em alta velocidade (dist > d)
    private Player findNearbyPlayer(LivingEntity mob) {
        Player p = null;
        double dist = 100.0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            double d = online.getLocation().distanceSquared(mob.getLocation());
            if (dist > d) {
                dist = d;
                p = online;
            }
        }
        return p;
    }

    @Override
    public void onDisable() {
        // Reseta todos os Creepers do servidor ao desligar o plugin por segurança
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Creeper creeper && creeper.isLeashed()) {
                    creeper.setMaxFuseTicks(30);
                }
            }
        }
    }
}
