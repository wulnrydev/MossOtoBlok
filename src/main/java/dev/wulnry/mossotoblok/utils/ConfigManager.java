package dev.wulnry.mossotoblok.utils;

import dev.wulnry.mossotoblok.MossOtoBlok;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Config Manager
 * 
 * Yapılandırma dosyasını yönetir
 * Materyal dönüşümlerini ve mesajları yükler
 * 
 * @author wulnrydev
 */
public class ConfigManager {

    private final MossOtoBlok plugin;
    private final Map<Material, Material> conversions;
    private int requiredAmount;

    public ConfigManager(MossOtoBlok plugin) {
        this.plugin = plugin;
        this.conversions = new HashMap<>();
        loadConfig();
    }

    /**
     * Yapılandırmayı yükle
     */
    public void loadConfig() {
        conversions.clear();
        
        // Gerekli materyal sayısını yükle
        requiredAmount = plugin.getConfig().getInt("required-amount", 9);
        
        // Dönüşümleri yükle
        ConfigurationSection conversionSection = plugin.getConfig().getConfigurationSection("conversions");
        if (conversionSection != null) {
            for (String key : conversionSection.getKeys(false)) {
                try {
                    Material from = Material.valueOf(key.toUpperCase());
                    Material to = Material.valueOf(conversionSection.getString(key).toUpperCase());
                    conversions.put(from, to);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + key);
                }
            }
        }
        
        plugin.getLogger().info(conversions.size() + " materyal dönüşümü yüklendi!");
    }

    /**
     * Yapılandırmayı yeniden yükle
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    /**
     * Dönüşüm haritasını döndür
     */
    public Map<Material, Material> getConversions() {
        return conversions;
    }

    /**
     * Gerekli materyal sayısını döndür
     */
    public int getRequiredAmount() {
        return requiredAmount;
    }

    /**
     * Config'den mesaj al
     * Prefix otomatik olarak eklenir
     */
    public String getMessage(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String message = plugin.getConfig().getString("messages." + key, "");
        return prefix + message;
    }

    /**
     * Config'den prefix olmadan mesaj al
     */
    public String getMessageWithoutPrefix(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }
}
