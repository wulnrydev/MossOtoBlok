package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Player Join Listener
 * 
 * Oyuncu sunucuya katıldığında tetiklenir
 * PDC verilerini yükler (gerekirse varsayılan değer atar)
 * 
 * @author wulnrydev
 */
public class PlayerJoinListener implements Listener {

    private final MossOtoBlok plugin;

    public PlayerJoinListener(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu sunucuya katıldığında PDC verilerini kontrol et
     * İlk kez giriş yapıyorsa varsayılan değer ata
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // PDC verilerini yükle/kontrol et
        // PlayerDataManager otomatik olarak yoksa varsayılan değer atar
        PlayerDataManager.isAutoBlockEnabled(event.getPlayer());
    }
}
