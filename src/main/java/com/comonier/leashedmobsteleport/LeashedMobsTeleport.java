package com.comonier.leashedmobsteleport;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LeashedMobsTeleport extends JavaPlugin {
    private NamespacedKey protectKey, ownerKey, wandLeashKey;
    private MessageManager messageManager;
    private MobUtils mobUtils;
    private ItemManager itemManager;
    private File playersFile;
    private FileConfiguration playersConfig;
    
    private final Set<UUID> disabledChatNotify = new HashSet<>();
    private final Set<UUID> disabledScreenNotify = new HashSet<>();
    private final Set<UUID> disabledActionNotify = new HashSet<>();
    private final Set<UUID> disabledBossNotify = new HashSet<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();

        this.playersFile = new File(getDataFolder(), "players.yml");
        this.playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        this.protectKey = new NamespacedKey(this, "LMT_PROTECTED");
        this.ownerKey = new NamespacedKey(this, "LMT_OWNER_UUID");
        this.wandLeashKey = new NamespacedKey(this, "LMT_WAND_LEASHED");
        
        this.messageManager = new MessageManager(this);
        this.mobUtils = new MobUtils(this);
        this.itemManager = new ItemManager(this);

        messageManager.loadMessages();
        loadPlayerData();

        if (getCommand("lmt") != null) {
            getCommand("lmt").setExecutor(new LMTCommand(this));
            getCommand("lmt").setTabCompleter(new LMTTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(new LeashListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WandSecurityListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionNotifyListener(this), this);

        new MobTicker(this).runTaskTimer(this, 10L, 10L);
    }

    public void loadPlayerData() {
        if (playersConfig.getConfigurationSection("players") == null) return;
        for (String uuidStr : playersConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            if (playersConfig.getBoolean("players." + uuidStr + ".chat")) disabledChatNotify.add(uuid);
            if (playersConfig.getBoolean("players." + uuidStr + ".screen")) disabledScreenNotify.add(uuid);
            if (playersConfig.getBoolean("players." + uuidStr + ".action")) disabledActionNotify.add(uuid);
            if (playersConfig.getBoolean("players." + uuidStr + ".boss")) disabledBossNotify.add(uuid);
        }
    }

    public void savePlayerData(UUID uuid) {
        String path = "players." + uuid.toString();
        playersConfig.set(path + ".chat", disabledChatNotify.contains(uuid));
        playersConfig.set(path + ".screen", disabledScreenNotify.contains(uuid));
        playersConfig.set(path + ".action", disabledActionNotify.contains(uuid));
        playersConfig.set(path + ".boss", disabledBossNotify.contains(uuid));
        try { playersConfig.save(playersFile); } catch (Exception e) { e.printStackTrace(); }
    }

    public NamespacedKey getProtectKey() { return protectKey; }
    public NamespacedKey getOwnerKey() { return ownerKey; }
    public NamespacedKey getWandLeashKey() { return wandLeashKey; }
    public MessageManager getMessageManager() { return messageManager; }
    public MobUtils getMobUtils() { return mobUtils; }
    public ItemManager getItemManager() { return itemManager; }
    
    public Set<UUID> getDisabledChatNotify() { return disabledChatNotify; }
    public Set<UUID> getDisabledScreenNotify() { return disabledScreenNotify; }
    public Set<UUID> getDisabledActionNotify() { return disabledActionNotify; }
    public Set<UUID> getDisabledBossNotify() { return disabledBossNotify; }

    @Override
    public void onDisable() {}
}
