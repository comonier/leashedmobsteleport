package com.comonier.leashedmobsteleport;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class LeashedMobsTeleport extends JavaPlugin {
    private NamespacedKey protectKey, ownerKey, wandLeashKey;
    private MessageManager messageManager;
    private MobUtils mobUtils;
    private ItemManager itemManager;
    private final Set<UUID> disabledMessages = new HashSet<>();
    @Override
    public void onEnable() {
        this.protectKey = new NamespacedKey(this, "LMT_PROTECTED");
        this.ownerKey = new NamespacedKey(this, "LMT_OWNER_UUID");
        this.wandLeashKey = new NamespacedKey(this, "LMT_WAND_LEASHED");
        this.messageManager = new MessageManager(this);
        this.mobUtils = new MobUtils(this);
        this.itemManager = new ItemManager(this);
        saveDefaultConfig();
        messageManager.loadMessages();
        if (null != getCommand("lmt")) getCommand("lmt").setExecutor(new LMTCommand(this));
        getServer().getPluginManager().registerEvents(new LeashListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WandSecurityListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionNotifyListener(this), this);
        new MobTicker(this).runTaskTimer(this, 10L, 10L);
    }
    public NamespacedKey getProtectKey() { return protectKey; }
    public NamespacedKey getOwnerKey() { return ownerKey; }
    public NamespacedKey getWandLeashKey() { return wandLeashKey; }
    public MessageManager getMessageManager() { return messageManager; }
    public MobUtils getMobUtils() { return mobUtils; }
    public ItemManager getItemManager() { return itemManager; }
    public Set<UUID> getDisabledMessages() { return disabledMessages; }
    @Override
    public void onDisable() {}
}
