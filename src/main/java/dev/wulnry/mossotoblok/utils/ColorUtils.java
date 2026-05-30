package dev.wulnry.mossotoblok.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color Utils
 * 
 * Renk kodlarını işler
 * HEX renk kodlarını (&#RRGGBB) destekler
 * Legacy renk kodlarını (&a, &7, vb.) destekler
 * 
 * @author wulnrydev
 */
public class ColorUtils {

    // HEX renk kodu pattern'i: &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Mesajı renklendir
     * HEX ve legacy renk kodlarını destekler
     * 
     * @param message Renklendirilecek mesaj
     * @return Renklendirilmiş mesaj
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // HEX renk kodlarını dönüştür
        message = translateHexColorCodes(message);
        
        // Legacy renk kodlarını dönüştür (&a, &7, vb.)
        message = translateLegacyColorCodes(message);
        
        return message;
    }

    /**
     * HEX renk kodlarını Minecraft formatına çevir
     * &#RRGGBB -> §x§R§R§G§G§B§B
     * 
     * @param message Mesaj
     * @return Dönüştürülmüş mesaj
     */
    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            
            for (char c : hexCode.toCharArray()) {
                replacement.append('§').append(c);
            }
            
            matcher.appendReplacement(buffer, replacement.toString());
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Legacy renk kodlarını dönüştür
     * & -> §
     * 
     * @param message Mesaj
     * @return Dönüştürülmüş mesaj
     */
    private static String translateLegacyColorCodes(String message) {
        return message.replace('&', '§');
    }

    /**
     * Component olarak renklendir (Adventure API)
     * 
     * @param message Mesaj
     * @return Component
     */
    public static Component colorizeComponent(String message) {
        String colored = colorize(message);
        return LegacyComponentSerializer.legacySection().deserialize(colored);
    }
}
