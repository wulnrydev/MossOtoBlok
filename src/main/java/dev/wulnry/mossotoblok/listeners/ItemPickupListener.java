package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.Collections;

/**
 * Item Pickup Listener
 *
 * Oyuncu eşya topladığında tetiklenir.
 * Event-driven mimari: Sadece eşya toplandığında çalışır.
 *
 * Optimizasyon: Toplanan materyal, oyuncu bazlı debounce/kuyruk sistemine
 * eklenir. Hızlı/çoklu pickup'lar (farm senaryoları) tek bir görevde birleşir,
 * scheduler spam'i oluşmaz.
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

        // Yalnızca toplanan eşyanın materyalini kontrol et (hedefli/optimize).
        // Kuyruğa eklenir: aynı tick'teki diğer pickup/click event'leriyle birleşir
        // ve eşya envantere eklendikten sonra (1 tick) tek görevde işlenir.
        Material picked = event.getItem().getItemStack().getType();
        plugin.getConversionManager().queueConversion(player, Collections.singleton(picked));
    }
}
