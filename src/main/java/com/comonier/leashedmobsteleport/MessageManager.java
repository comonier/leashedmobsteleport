package com.comonier.leashedmobsteleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class MessageManager {
    private final LeashedMobsTeleport plugin;
    private FileConfiguration msgConfig;

    public MessageManager(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        // Força a existência de todos os arquivos suportados
        String[] langs = {"en", "pt", "es", "ru"};
        for (String l : langs) {
            saveResourceIfNotExists("messages_" + l + ".yml");
        }

        String lang = plugin.getConfig().getString("language", "en");
        String fileName = "messages_" + lang + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);

        // Se o arquivo sumiu entre o save e o load, usa o en
        if (!file.exists()) {
            file = new File(plugin.getDataFolder(), "messages_en.yml");
            fileName = "messages_en.yml";
        }

        msgConfig = YamlConfiguration.loadConfiguration(file);
        checkIntegrity(file, fileName);
    }

    private void saveResourceIfNotExists(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                plugin.saveResource(name, false);
            } catch (Exception ignored) {}
        }
    }

    private void checkIntegrity(File file, String fileName) {
        try {
            // Tenta ler o arquivo original dentro do JAR para comparar
            var resource = plugin.getResource(fileName);
            if (resource == null) return;

            Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(reader);

            boolean corrupt = false;
            // Verifica se todas as chaves do JAR existem no arquivo da pasta
            for (String key : internalConfig.getKeys(true)) {
                if (!msgConfig.contains(key)) {
                    corrupt = true;
                    break;
                }
            }

            if (corrupt) {
                String errorMsg = "§c[LMT] The file " + fileName + " is corrupt or missing keys! Using internal backup.";
                Bukkit.getConsoleSender().sendMessage(errorMsg);
                plugin.getLogger().warning("File " + fileName + " is corrupt! Please delete it to regenerate.");
                
                // Em caso de corrupção, sobrepomos o config em memória com o do JAR
                msgConfig = internalConfig;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking integrity of " + fileName);
        }
    }

    public String getMsg(String key) {
        String lang = plugin.getConfig().getString("language", "en");
        
        // Tenta nó de idioma (pt.key) ou raiz (key)
        String raw = msgConfig.getString(lang + "." + key, msgConfig.getString(key));

        if (raw == null) {
            return "§cMissing key: " + key;
        }

        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
