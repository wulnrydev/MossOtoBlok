// src/main/java/dev/wulnry/mossotoblok/listeners/PlayerListener.java
package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Player Listener
 *
 * Oyuncu sunucudan ayrıldığında (çıkış veya atılma) bekleyen dönüşüm verilerini
 * temizler (KRİTİK-1). {@link dev.wulnry.mossotoblok.utils.ConversionManager}
 * içindeki UUID bazlı haritalar temizlenmezse 400+ oyunculu ortamda zamanla
 * heap memory leak oluşur.
 *
 * @author wulnrydev
 */
public class PlayerListener implements Listener {

    private final MossOtoBlok plugin;

    /**
     * Yeni bir {@link PlayerListener} oluşturur.
     *
     * @param plugin Ana eklenti örneği
     */
    public PlayerListener(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu normal şekilde çıktığında bekleyen verileri temizler.
     *
     * @param event Çıkış olayı
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clear(event.getPlayer().getUniqueId());
    }

    /**
     * Oyuncu sunucudan atıldığında bekleyen verileri temizler.
     *
     * @param event Atılma olayı
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clear(event.getPlayer().getUniqueId());
    }

    /**
     * İlgili UUID için bekleyen materyalleri ve görevi temizler (idempotent).
     */
    private void clear(UUID id) {
        plugin.getConversionManager().clearPending(id);
    }
}
