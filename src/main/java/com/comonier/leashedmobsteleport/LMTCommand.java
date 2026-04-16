package com.comonier.leashedmobsteleport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.UUID;

public class LMTCommand implements CommandExecutor {
    private final LeashedMobsTeleport plugin;

    public LMTCommand(LeashedMobsTeleport plugin) { 
        this.plugin = plugin; 
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) { 
            sendInfo(sender); 
            return true; 
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("leashedmobsteleport.admin")) {
                sender.sendMessage(plugin.getMessageManager().getMsg("no_permission_admin"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getMessageManager().loadMessages();
            sender.sendMessage(plugin.getMessageManager().getMsg("reload_success"));
            return true;
        }

        if (sub.equals("chat") || sub.equals("screen") || sub.equals("action") || sub.equals("boss")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessageManager().getMsg("only_players"));
                return true;
            }

            Player p = (Player) sender;
            String configPath = "notifications." + sub;

            if (!plugin.getConfig().getBoolean(configPath, true)) {
                p.sendMessage(plugin.getMessageManager().getMsg("notify_disabled_by_admin"));
                return true;
            }

            handleToggle(p, sub);
            return true;
        }

        if (sub.equals("wand")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessageManager().getMsg("only_players"));
                return true;
            }
            
            Player p = (Player) sender;
            if (!p.hasPermission("leashedmobsteleport.use")) {
                p.sendMessage(plugin.getMessageManager().getMsg("no_permission"));
                return true;
            }

            boolean hasWand = false;
            int size = p.getInventory().getSize();
            for (int i = 0; i != size; i++) {
                ItemStack is = p.getInventory().getItem(i);
                if (plugin.getItemManager().isWand(is)) {
                    p.getInventory().setItem(i, null);
                    hasWand = true;
                }
            }

            if (hasWand) {
                p.sendMessage(plugin.getMessageManager().getMsg("wand_remove"));
            } else {
                p.getInventory().addItem(plugin.getItemManager().getEnchantedWand());
                p.sendMessage(plugin.getMessageManager().getMsg("wand_receive"));
                p.sendMessage(plugin.getMessageManager().getMsg("wand_tip"));
            }
            return true;
        }

        sendInfo(sender);
        return true;
    }

    private void handleToggle(Player p, String type) {
        Set<UUID> list;
        String msgKeyOn, msgKeyOff;

        if (type.equals("chat")) {
            list = plugin.getDisabledChatNotify();
            msgKeyOn = "chat_on";
            msgKeyOff = "chat_off";
        } else if (type.equals("screen")) {
            list = plugin.getDisabledScreenNotify();
            msgKeyOn = "screen_on";
            msgKeyOff = "screen_off";
        } else if (type.equals("action")) {
            list = plugin.getDisabledActionNotify();
            msgKeyOn = "action_on";
            msgKeyOff = "action_off";
        } else if (type.equals("boss")) {
            list = plugin.getDisabledBossNotify();
            msgKeyOn = "boss_on";
            msgKeyOff = "boss_off";
        } else {
            return;
        }

        UUID uuid = p.getUniqueId();
        if (list.contains(uuid)) {
            list.remove(uuid);
            p.sendMessage(plugin.getMessageManager().getMsg(msgKeyOn));
        } else {
            list.add(uuid);
            p.sendMessage(plugin.getMessageManager().getMsg(msgKeyOff));
        }
        
        // Salva a alteração permanentemente no players.yml
        plugin.savePlayerData(uuid);
    }

    private void sendInfo(CommandSender s) {
        String st = plugin.getConfig().getBoolean("use-permission-per-mob", false) ? plugin.getMessageManager().getMsg("enabled") : plugin.getMessageManager().getMsg("disabled");
        s.sendMessage(plugin.getMessageManager().getMsg("info_header"));
        s.sendMessage(plugin.getMessageManager().getMsg("info_version").replace("%version%", plugin.getDescription().getVersion()));
        s.sendMessage(plugin.getMessageManager().getMsg("info_perm_status").replace("%status%", st));
        s.sendMessage(plugin.getMessageManager().getMsg("info_commands"));
        s.sendMessage(plugin.getMessageManager().getMsg("info_links"));
    }
}
