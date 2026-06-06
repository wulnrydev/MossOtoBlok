package dev.wulnry.mossotoblok.listeners;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();

        // PDC verilerini yükle/kontrol et
        // PlayerDataManager otomatik olarak yoksa varsayılan değer atar
        boolean enabled = PlayerDataManager.isAutoBlockEnabled(player);

        // Sistem açıksa, önceden/çevrimdışı birikmiş yığınları bir kez tara.
        // Bu tek seferlik bir taramadır; arka plan döngüsü değildir (zero-lag korunur).
        if (enabled) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                plugin.getConversionManager().checkAndConvertItems(player), 1L);
        }
    }
}
