package com.comonier.leashedmobsteleport;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    private final LeashedMobsTeleport plugin;
    private final NamespacedKey wandKey;

    public ItemManager(LeashedMobsTeleport plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "LMT_ENCHANTED_WAND");
    }

    public ItemStack getEnchantedWand() {
        ItemStack wand = new ItemStack(Material.LEAD);
        ItemMeta meta = wand.getItemMeta();
        
        if (null != meta) {
            meta.setDisplayName("§b§lEnchanted Lead §7(LMT Wand)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Item vinculado à alma.");
            lore.add("§7Use para laçar qualquer mob.");
            lore.add("§7Clique no mob na cerca para resgatá-lo.");
            lore.add("§e§oEste item não pode ser descartado.");
            meta.setLore(lore);
            
            // DURABILITY é o nome universal para Unbreaking em versões antigas e novas da API
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (null == item || Material.LEAD != item.getType() || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }
}
