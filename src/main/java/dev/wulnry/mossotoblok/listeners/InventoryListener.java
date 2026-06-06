package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Inventory Listener
 *
 * Oyuncu envanterinde eşya taşıdığında tetiklenir
 * Event-driven mimari: Sadece envanter değiştiğinde çalışır
 *
 * Optimizasyon: Yalnızca olayın etkilediği materyal(ler) kontrol edilir,
 * tüm dönüşümler taranmaz.
 *
 * @author wulnrydev
 */
public class InventoryListener implements Listener {

    private final MossOtoBlok plugin;

    public InventoryListener(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu envanterde eşya tıkladığında kontrol et.
     * HIGHEST priority (ORTA-3): MONITOR yerine kullanılır; izleme amaçlı en son
     * çalışan MONITOR seviyesi 400+ oyuncuda gereksiz gecikme yaratırdı.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            return;
        }

        // Tıklamada bir kaynağın sayısı yalnızca tıklanan slottaki ya da
        // imleçteki materyal için artabilir → yalnızca onları kontrol et.
        Set<Material> candidates = new HashSet<>();
        addType(candidates, event.getCurrentItem());
        addType(candidates, event.getCursor());

        plugin.getConversionManager().queueConversion(player, candidates);
    }

    /**
     * Oyuncu envanterde eşya sürüklediğinde kontrol et.
     * HIGHEST priority (ORTA-3): MONITOR yerine kullanılır.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            return;
        }

        // Sürüklenen (imleçteki) materyal kontrol edilir.
        Set<Material> candidates = new HashSet<>();
        addType(candidates, event.getOldCursor());
        for (ItemStack item : event.getNewItems().values()) {
            addType(candidates, item);
        }

        plugin.getConversionManager().queueConversion(player, candidates);
    }

    /**
     * Aday materyali (null/AIR olmayan) sete ekle.
     */
    private void addType(Set<Material> set, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            set.add(item.getType());
        }
    }
}
