package com.comonier.leashedmobsteleport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LMTTabCompleter implements TabCompleter {
    private final LeashedMobsTeleport plugin;

    public LMTTabCompleter(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("chat");
            suggestions.add("screen");
            suggestions.add("action");
            suggestions.add("boss");
            suggestions.add("wand");

            if (sender.hasPermission("leashedmobsteleport.admin")) {
                suggestions.add("reload");
            }

            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
