// src/main/java/dev/wulnry/mossotoblok/utils/ConfigManager.java
package dev.wulnry.mossotoblok.utils;

import dev.wulnry.mossotoblok.MossOtoBlok;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Config Manager
 *
 * Yapılandırma dosyasını yönetir.
 * Materyal dönüşümlerini ve mesajları yükler.
 *
 * @author wulnrydev
 */
public class ConfigManager {

    /** Tek dönüşümde düşürülebilecek varsayılan üst stack limiti. */
    private static final int DEFAULT_MAX_DROP_STACKS = 5;

    /** Varsayılan prefix — moda özel değil, jenerik MossOtoBlok prefix'i (DÜŞÜK-6). */
    private static final String DEFAULT_PREFIX = "&#00ff00&lMossOtoBlok &8▸ ";

    private final MossOtoBlok plugin;
    private final Map<Material, Material> conversions;
    // Mesaj önbelleği: event/komut sırasında config'e erişim olmaz, doğrudan RAM'den okunur.
    // reload komutunda loadConfig() ile yeniden inşa edilir.
    private final Map<String, String> messages;
    private String prefix;
    private int requiredAmount;
    private int maxDropStacks;

    /**
     * Yeni bir {@link ConfigManager} oluşturur ve yapılandırmayı hemen yükler.
     *
     * @param plugin Ana eklenti örneği
     */
    public ConfigManager(MossOtoBlok plugin) {
        this.plugin = plugin;
        this.conversions = new HashMap<>();
        this.messages = new HashMap<>();
        loadConfig();
    }

    /**
     * Yapılandırmayı diskten okuyup önbellekleri yeniden inşa eder.
     */
    public void loadConfig() {
        conversions.clear();
        messages.clear();

        // Gerekli materyal sayısını yükle
        requiredAmount = plugin.getConfig().getInt("required-amount", 9);

        // ORTA-5: Tek dönüşümde yere düşürülebilecek üst stack limiti.
        maxDropStacks = plugin.getConfig().getInt("max-drop-stacks", DEFAULT_MAX_DROP_STACKS);
        if (maxDropStacks < 0) {
            maxDropStacks = 0;
        }

        // Mesajları önbelleğe al (prefix dahil) → event/komut sırasında config okunmaz.
        // DÜŞÜK-6: Prefix admin tarafından değiştirilebilir; yoksa jenerik varsayılan kullanılır.
        prefix = plugin.getConfig().getString("messages.prefix", DEFAULT_PREFIX);
        ConfigurationSection messageSection = plugin.getConfig().getConfigurationSection("messages");
        if (messageSection != null) {
            for (String key : messageSection.getKeys(false)) {
                messages.put(key, messageSection.getString(key, ""));
            }
        }

        // Dönüşümleri yükle
        ConfigurationSection conversionSection = plugin.getConfig().getConfigurationSection("conversions");
        if (conversionSection != null) {
            for (String key : conversionSection.getKeys(false)) {
                String value = conversionSection.getString(key);
                if (value == null || value.trim().isEmpty()) {
                    plugin.getLogger().warning("Dönüşüm hedefi boş: " + key + " (atlanıyor)");
                    continue;
                }

                try {
                    Material from = Material.valueOf(key.toUpperCase());
                    Material to = Material.valueOf(value.toUpperCase());

                    if (from == to) {
                        plugin.getLogger().warning("Kaynak ve hedef aynı: " + key + " (atlanıyor)");
                        continue;
                    }

                    conversions.put(from, to);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + key + " -> " + value + " (atlanıyor)");
                }
            }
        }

        plugin.getLogger().info(conversions.size() + " materyal dönüşümü yüklendi!");
    }

    /**
     * Yapılandırmayı diskten yeniden yükler.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    /**
     * Dönüşüm haritasını döndürür.
     *
     * @return Kaynak materyalden hedef bloğa eşleme
     */
    public Map<Material, Material> getConversions() {
        return conversions;
    }

    /**
     * Bir dönüşüm için gereken kaynak materyal miktarını döndürür.
     *
     * @return Gerekli materyal sayısı
     */
    public int getRequiredAmount() {
        return requiredAmount;
    }

    /**
     * Tek bir dönüşüm işleminde yere düşürülebilecek üst stack limitini döndürür (ORTA-5).
     *
     * @return İzin verilen maksimum düşürülen stack sayısı
     */
    public int getMaxDropStacks() {
        return maxDropStacks;
    }

    /**
     * Önbellekten mesaj alır; başına prefix otomatik eklenir.
     * Config dosyasına erişmez; tüm değerler açılışta/reload'da yüklenir.
     *
     * @param key Mesaj anahtarı
     * @return Prefix'li mesaj (anahtar yoksa yalnızca prefix)
     */
    public String getMessage(String key) {
        return prefix + messages.getOrDefault(key, "");
    }

    /**
     * Önbellekten prefix olmadan mesaj alır.
     *
     * @param key Mesaj anahtarı
     * @return Prefix'siz ham mesaj (anahtar yoksa boş metin)
     */
    public String getMessageWithoutPrefix(String key) {
        return messages.getOrDefault(key, "");
    }
}
