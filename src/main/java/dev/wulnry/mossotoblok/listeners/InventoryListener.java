package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.ConversionManager;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Inventory Listener
 * 
 * Oyuncu envanterinde eşya taşıdığında tetiklenir
 * Event-driven mimari: Sadece envanter değiştiğinde çalışır
 * 
 * @author wulnrydev
 */
public class InventoryListener implements Listener {

    private final MossOtoBlok plugin;

    public InventoryListener(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu envanterde eşya tıkladığında kontrol et
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Oto-blok sistemi kapalıysa işlem yapma
        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            return;
        }

        // Eşya taşındıktan sonra kontrol et
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConversionManager conversionManager = plugin.getConversionManager();
            conversionManager.checkAndConvertItems(player);
        }, 1L);
    }

    /**
     * Oyuncu envanterde eşya sürüklediğinde kontrol et
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Oto-blok sistemi kapalıysa işlem yapma
        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            return;
        }

        // Eşya sürüklendikten sonra kontrol et
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConversionManager conversionManager = plugin.getConversionManager();
            conversionManager.checkAndConvertItems(player);
        }, 1L);
    }
}
