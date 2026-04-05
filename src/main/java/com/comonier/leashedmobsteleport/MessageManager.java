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
        String lang = plugin.getConfig().getString("language", "en");
        String fileName = "messages_" + lang + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                // Se o arquivo não existir no JAR (ex: messages_ru.yml), usa o padrão 'en'
                plugin.getLogger().warning("Arquivo de tradução '" + fileName + "' não encontrado no JAR. Usando padrão (en).");
                if (!lang.equals("en")) {
                    plugin.getConfig().set("language", "en");
                    loadMessages(); // Tenta carregar o inglês
                    return;
                }
            }
        }
        msgConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMsg(String key) {
        String lang = plugin.getConfig().getString("language", "en");
        String raw = msgConfig.getString(lang + "." + key);
        if (null == raw) raw = msgConfig.getString(key, "Missing key: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
