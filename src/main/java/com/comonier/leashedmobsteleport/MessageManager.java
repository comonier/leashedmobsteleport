package com.comonier.leashedmobsteleport;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MessageManager {
    private final LeashedMobsTeleport plugin;
    private FileConfiguration msgConfig;

    public MessageManager(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) plugin.saveResource("messages.yml", false);
        msgConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    public String getMsg(String key) {
        String lang = plugin.getConfig().getString("language", "en");
        String raw = msgConfig.getString(lang + "." + key, "Missing: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
