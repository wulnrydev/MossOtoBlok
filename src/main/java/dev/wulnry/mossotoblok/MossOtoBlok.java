package dev.wulnry.mossotoblok;

import dev.wulnry.mossotoblok.commands.OtoBlokCommand;
import dev.wulnry.mossotoblok.listeners.ItemPickupListener;
import dev.wulnry.mossotoblok.listeners.InventoryListener;
import dev.wulnry.mossotoblok.listeners.PlayerJoinListener;
import dev.wulnry.mossotoblok.utils.ConfigManager;
import dev.wulnry.mossotoblok.utils.ConversionManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MossOtoBlok - Ana Eklenti Sınıfı
 * 
 * Otomatik blok yapma sistemi - Sıfır lag, maksimum optimizasyon
 * Event-driven mimari ile çalışır, arka planda sürekli döngü yoktur
 * 
 * @author wulnrydev
 * @version 1.0.0
 */
public final class MossOtoBlok extends JavaPlugin {

    private static MossOtoBlok instance;
    private ConfigManager configManager;
    private ConversionManager conversionManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Yapılandırma dosyalarını yükle
        saveDefaultConfig();
        
        // Manager'ları başlat
        this.configManager = new ConfigManager(this);
        this.conversionManager = new ConversionManager(this);
        
        // Komutları kaydet
        registerCommands();
        
        // Event listener'ları kaydet
        registerListeners();
        
        getLogger().info("MossOtoBlok başarıyla yüklendi! (v1.0.0)");
        getLogger().info("Geliştirici: wulnrydev");
    }

    @Override
    public void onDisable() {
        getLogger().info("MossOtoBlok kapatıldı!");
    }

    /**
     * Komutları kaydet
     */
    private void registerCommands() {
        getCommand("otoblok").setExecutor(new OtoBlokCommand(this));
    }

    /**
     * Event listener'ları kaydet
     * Event-driven mimari: Sadece gerekli anlarda tetiklenir
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    /**
     * Eklenti instance'ını döndür
     */
    public static MossOtoBlok getInstance() {
        return instance;
    }

    /**
     * Config manager'ı döndür
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Conversion manager'ı döndür
     */
    public ConversionManager getConversionManager() {
        return conversionManager;
    }
}
