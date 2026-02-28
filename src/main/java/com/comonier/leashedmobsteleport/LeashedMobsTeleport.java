package com.comonier.leashedmobsteleport;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LeashedMobsTeleport extends JavaPlugin {

    private NamespacedKey protectKey;
    private NamespacedKey ownerKey;
    private MessageManager messageManager;
    private MobUtils mobUtils;
    private ItemManager itemManager;
    private final Set<UUID> disabledMessages = new HashSet<>();

    @Override
    public void onEnable() {
        // 1. Inicializar as Chaves de DNA (PDC)
        this.protectKey = new NamespacedKey(this, "LMT_PROTECTED");
        this.ownerKey = new NamespacedKey(this, "LMT_OWNER_UUID");
        
        // 2. Inicializar Gerenciadores
        this.messageManager = new MessageManager(this);
        this.mobUtils = new MobUtils(this);
        this.itemManager = new ItemManager(this);

        // 3. Carregar Configurações e Mensagens
        saveDefaultConfig();
        messageManager.loadMessages();

        // 4. Registrar Comando Principal
        if (getCommand("lmt") != null) {
            getCommand("lmt").setExecutor(new LMTCommand(this));
        }

        // 5. Registrar Todos os Listeners (Módulos de Eventos)
        getServer().getPluginManager().registerEvents(new LeashListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WandSecurityListener(this), this);

        // 6. Iniciar a Task de Monitoramento (Ticker de 10 ticks conforme v1.5.5)
        new MobTicker(this).runTaskTimer(this, 10L, 10L);

        getLogger().info("LeashedMobsTeleport v1.5.6 (DNA Persistente + Wand) ativado com sucesso!");
    }

    // Getters para que os outros arquivos acessem as funções centrais
    public NamespacedKey getProtectKey() { 
        return protectKey; 
    }
    
    public NamespacedKey getOwnerKey() { 
        return ownerKey; 
    }
    
    public MessageManager getMessageManager() { 
        return messageManager; 
    }
    
    public MobUtils getMobUtils() { 
        return mobUtils; 
    }
    
    public ItemManager getItemManager() { 
        return itemManager; 
    }
    
    public Set<UUID> getDisabledMessages() { 
        return disabledMessages; 
    }

    @Override
    public void onDisable() {
        // As tags do DNA (PDC) permanecem nos mobs para o próximo reinício.
        getLogger().info("LeashedMobsTeleport desativado. Tags de DNA preservadas.");
    }
}
