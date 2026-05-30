package dev.wulnry.mossotoblok.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Player Data Manager
 * 
 * Oyuncu verilerini PersistentDataContainer (PDC) ile yönetir
 * 
 * Avantajlar:
 * - Veritabanı gerektirmez
 * - YML dosyası gerektirmez
 * - Oyuncu ile birlikte saklanır
 * - Performanslı ve hafif
 * 
 * @author wulnrydev
 */
public class PlayerDataManager {

    private static final NamespacedKey AUTO_BLOCK_KEY = 
        new NamespacedKey("mossotoblok", "auto_block_enabled");

    /**
     * Oyuncunun oto-blok sisteminin açık olup olmadığını kontrol et
     * 
     * @param player Kontrol edilecek oyuncu
     * @return Sistem açıksa true, kapalıysa false
     */
    public static boolean isAutoBlockEnabled(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        
        // Veri yoksa varsayılan olarak true (açık)
        if (!data.has(AUTO_BLOCK_KEY, PersistentDataType.BOOLEAN)) {
            data.set(AUTO_BLOCK_KEY, PersistentDataType.BOOLEAN, true);
            return true;
        }
        
        return data.get(AUTO_BLOCK_KEY, PersistentDataType.BOOLEAN);
    }

    /**
     * Oyuncunun oto-blok sistemini aç/kapat
     * 
     * @param player Ayarlanacak oyuncu
     * @param enabled Açık mı kapalı mı
     */
    public static void setAutoBlockEnabled(Player player, boolean enabled) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(AUTO_BLOCK_KEY, PersistentDataType.BOOLEAN, enabled);
    }
}
