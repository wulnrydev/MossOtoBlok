package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.ConversionManager;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Item Pickup Listener
 * 
 * Oyuncu eşya topladığında tetiklenir
 * Event-driven mimari: Sadece eşya toplandığında çalışır
 * 
 * @author wulnrydev
 */
public class ItemPickupListener implements Listener {

    private final MossOtoBlok plugin;

    public ItemPickupListener(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu eşya topladığında kontrol et
     * HIGHEST priority: Diğer eklentilerden sonra çalışır
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        // Sadece oyuncular için
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Oto-blok sistemi kapalıysa işlem yapma
        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            return;
        }

        // Eşya toplandıktan sonra envanter kontrolü yap
        // 1 tick bekle ki eşya envantere eklensin
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConversionManager conversionManager = plugin.getConversionManager();
            conversionManager.checkAndConvertItems(player);
        }, 1L);
    }
}
