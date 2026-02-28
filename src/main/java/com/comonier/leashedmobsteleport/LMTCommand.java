package com.comonier.leashedmobsteleport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LMTCommand implements CommandExecutor {
    private final LeashedMobsTeleport plugin;

    public LMTCommand(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (0 == args.length) {
            sendPluginInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (false == sender.hasPermission("leashedmobsteleport.admin")) {
                sender.sendMessage(plugin.getMessageManager().getMsg("no_permission_admin"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getMessageManager().loadMessages();
            sender.sendMessage(plugin.getMessageManager().getMsg("reload_success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (sender instanceof Player p) {
                if (plugin.getDisabledMessages().contains(p.getUniqueId())) {
                    plugin.getDisabledMessages().remove(p.getUniqueId());
                    p.sendMessage(plugin.getMessageManager().getMsg("toggle_on"));
                } else {
                    plugin.getDisabledMessages().add(p.getUniqueId());
                    p.sendMessage(plugin.getMessageManager().getMsg("toggle_off"));
                }
                return true;
            }
            sender.sendMessage("§cApenas jogadores podem usar o comando toggle.");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand")) {
            if (sender instanceof Player p) {
                if (false == p.hasPermission("leashedmobsteleport.use")) {
                    p.sendMessage(plugin.getMessageManager().getMsg("no_permission"));
                    return true;
                }

                boolean possuiWand = false;
                // Varre o inventário para verificar se já existe uma Wand e a remove
                for (int i = 0; p.getInventory().getSize() > i; i++) {
                    ItemStack is = p.getInventory().getItem(i);
                    if (plugin.getItemManager().isWand(is)) {
                        p.getInventory().setItem(i, null);
                        possuiWand = true;
                    }
                }

                if (possuiWand) {
                    p.sendMessage("§b§lLMT §8» §cEnchanted Lead removida do seu inventário.");
                } else {
                    p.getInventory().addItem(plugin.getItemManager().getEnchantedWand());
                    p.sendMessage("§b§lLMT §8» §aVocê recebeu a §bEnchanted Lead§a!");
                    p.sendMessage("§7§oDica: Clique no mob preso na cerca para resgatá-lo.");
                }
                return true;
            }
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        sendPluginInfo(sender);
        return true;
    }

    private void sendPluginInfo(CommandSender sender) {
        String status = plugin.getConfig().getBoolean("use-permission-per-mob", false) 
                        ? plugin.getMessageManager().getMsg("enabled") 
                        : plugin.getMessageManager().getMsg("disabled");
                        
        sender.sendMessage(plugin.getMessageManager().getMsg("info_header"));
        sender.sendMessage(plugin.getMessageManager().getMsg("info_version").replace("%version%", plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.getMessageManager().getMsg("info_perm_status").replace("%status%", status));
        sender.sendMessage("§7Comandos: §f/lmt reload, toggle, wand");
        sender.sendMessage(plugin.getMessageManager().getMsg("info_links"));
    }
}
