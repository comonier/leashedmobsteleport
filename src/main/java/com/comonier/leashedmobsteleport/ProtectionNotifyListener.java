package com.comonier.leashedmobsteleport;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.HashMap;
import java.util.UUID;

public class ProtectionNotifyListener implements Listener {
    private final LeashedMobsTeleport plugin;
    private final HashMap<UUID, Long> lastClaimId = new HashMap<>();
    private final HashMap<UUID, Boolean> lastTrustStatus = new HashMap<>();
    private final HashMap<UUID, BossBar> activeBars = new HashMap<>();

    public ProtectionNotifyListener(LeashedMobsTeleport plugin) { 
        this.plugin = plugin; 
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) return;

        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())) return;

        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        try {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);

            if (claim == null) {
                if (lastClaimId.containsKey(uuid)) {
                    sendExitNotify(p);
                    lastClaimId.remove(uuid);
                    lastTrustStatus.remove(uuid);
                }
                return;
            }

            long currentId = claim.getID();
            boolean hasTrust = (claim.allowAccess(p) == null);

            if (!lastClaimId.containsKey(uuid) || lastClaimId.get(uuid) != currentId) {
                sendEnterNotify(p, claim.getOwnerName(), hasTrust);
                lastClaimId.put(uuid, currentId);
                lastTrustStatus.put(uuid, hasTrust);
                return;
            }

            if (lastTrustStatus.get(uuid) != hasTrust) {
                sendTrustChangeNotify(p, hasTrust);
                lastTrustStatus.put(uuid, hasTrust);
            }
        } catch (Throwable ignored) {}
    }

    private void sendEnterNotify(Player p, String owner, boolean hasTrust) {
        UUID uuid = p.getUniqueId();
        String chatMain = plugin.getMessageManager().getMsg("notify_enter_claim").replace("%owner%", owner);
        String chatSub = plugin.getMessageManager().getMsg(hasTrust ? "notify_has_trust" : "notify_no_trust");

        // CHAT (Com prefixo vindo do YML)
        if (plugin.getConfig().getBoolean("notifications.chat", true) && !plugin.getDisabledChatNotify().contains(uuid)) {
            p.sendMessage(chatMain);
            p.sendMessage(chatSub);
        }

        // SCREEN (Sem prefixo vindo do YML)
        if (plugin.getConfig().getBoolean("notifications.screen", true) && !plugin.getDisabledScreenNotify().contains(uuid)) {
            p.sendTitle(owner, chatSub.replace("§b§lLMT §8» ", ""), 10, 40, 10);
        }

        // ACTION BAR
        if (plugin.getConfig().getBoolean("notifications.actionbar", false) && !plugin.getDisabledActionNotify().contains(uuid)) {
            String act = plugin.getMessageManager().getMsg(hasTrust ? "notify_action_trusted" : "notify_action_untrusted").replace("%owner%", owner);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(act));
        }
    }

    private void sendExitNotify(Player p) {
        String msg = plugin.getMessageManager().getMsg("notify_exit_claim");
        if (plugin.getConfig().getBoolean("notifications.chat", true) && !plugin.getDisabledChatNotify().contains(p.getUniqueId())) {
            p.sendMessage(msg);
        }
        if (plugin.getConfig().getBoolean("notifications.screen", true) && !plugin.getDisabledScreenNotify().contains(p.getUniqueId())) {
            p.sendTitle("", msg.replace("§b§lLMT §8» ", ""), 10, 40, 10);
        }
    }

    private void sendTrustChangeNotify(Player p, boolean hasTrust) {
        String msg = plugin.getMessageManager().getMsg(hasTrust ? "notify_trust_gain" : "notify_trust_loss");
        p.sendMessage(msg);
    }
}
