package com.comonier.leashedmobsteleport;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
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
    public ProtectionNotifyListener(LeashedMobsTeleport plugin) { this.plugin = plugin; }
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (null == to || (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())) return;
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if (null == claim) {
            if (lastClaimId.containsKey(uuid)) {
                p.sendMessage("§b§lLMT §8» §fVocê saiu de uma área protegida.");
                lastClaimId.remove(uuid);
                lastTrustStatus.remove(uuid);
            }
            return;
        }
        long currentId = claim.getID();
        boolean hasTrust = (null == claim.allowAccess(p));
        if (false == lastClaimId.containsKey(uuid) || lastClaimId.get(uuid) != currentId) {
            p.sendMessage("§b§lLMT §8» §fEntrou no terreno de: §a" + claim.getOwnerName());
            if (hasTrust) p.sendMessage("§b§lLMT §8» §aVocê tem permissão de acesso aqui.");
            else p.sendMessage("§b§lLMT §8» §cVocê não tem permissão aqui.");
            lastClaimId.put(uuid, currentId);
            lastTrustStatus.put(uuid, hasTrust);
            return;
        }
        if (lastTrustStatus.get(uuid) != hasTrust) {
            if (hasTrust) p.sendMessage("§b§lLMT §8» §aSeu acesso foi concedido neste terreno!");
            else p.sendMessage("§b§lLMT §8» §cSeu acesso foi removido deste terreno!");
            lastTrustStatus.put(uuid, hasTrust);
        }
    }
}
