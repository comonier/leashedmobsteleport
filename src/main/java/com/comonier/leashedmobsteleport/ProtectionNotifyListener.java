package com.comonier.leashedmobsteleport;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.UUID;

public class ProtectionNotifyListener implements Listener {
    private final LeashedMobsTeleport plugin;
    private final HashMap<UUID, Long> lastClaimId = new HashMap<>();
    private final HashMap<UUID, Boolean> lastTrustStatus = new HashMap<>();

    public ProtectionNotifyListener(LeashedMobsTeleport plugin) { 
        this.plugin = plugin; 
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Se o GriefPrevention não estiver instalado, sai imediatamente para evitar o NoClassDefFoundError
        if (!Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Só processa se mudar de bloco X ou Z (ignora Y e visão)
        if (null == to || (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())) return;

        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        try {
            // Lógica do GriefPrevention envolta em try-catch por segurança extra
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);

            if (null == claim) {
                if (lastClaimId.containsKey(uuid)) {
                    p.sendMessage(plugin.getMessageManager().getMsg("notify_exit_claim"));
                    lastClaimId.remove(uuid);
                    lastTrustStatus.remove(uuid);
                }
                return;
            }

            long currentId = claim.getID();
            boolean hasTrust = (null == claim.allowAccess(p));

            // Se entrou em um novo claim ou mudou de claim
            if (false == lastClaimId.containsKey(uuid) || lastClaimId.get(uuid) != currentId) {
                p.sendMessage(plugin.getMessageManager().getMsg("notify_enter_claim").replace("%owner%", claim.getOwnerName()));
                p.sendMessage(plugin.getMessageManager().getMsg(hasTrust ? "notify_has_trust" : "notify_no_trust"));
                lastClaimId.put(uuid, currentId);
                lastTrustStatus.put(uuid, hasTrust);
                return;
            }

            // Se o status de trust mudou dentro do mesmo claim
            if (lastTrustStatus.get(uuid) != hasTrust) {
                p.sendMessage(plugin.getMessageManager().getMsg(hasTrust ? "notify_trust_gain" : "notify_trust_loss"));
                lastTrustStatus.put(uuid, hasTrust);
            }
        } catch (Throwable ignored) {
            // Silencia qualquer erro caso o plugin seja removido em runtime
        }
    }
}
