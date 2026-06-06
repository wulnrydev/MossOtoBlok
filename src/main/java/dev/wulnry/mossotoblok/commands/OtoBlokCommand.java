package dev.wulnry.mossotoblok.commands;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.ColorUtils;
import dev.wulnry.mossotoblok.utils.PlayerDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * OtoBlok Komutu
 * 
 * Komut: /otoblok <aç|kapat>
 * Yetki: Yok (Herkese açık)
 * 
 * Oyuncuların oto-blok sistemini açıp kapatmalarını sağlar
 * 
 * @author wulnrydev
 */
public class OtoBlokCommand implements CommandExecutor {

    private final MossOtoBlok plugin;

    public OtoBlokCommand(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        // Sadece oyuncular kullanabilir
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir!");
            return true;
        }

        // Argüman kontrolü
        if (args.length != 1) {
            sendUsageMessage(player);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "aç", "ac", "enable", "on" -> {
                // Oto-blok sistemini aç
                PlayerDataManager.setAutoBlockEnabled(player, true);
                sendMessage(player, "enabled");
            }
            case "kapat", "disable", "off" -> {
                // Oto-blok sistemini kapat
                PlayerDataManager.setAutoBlockEnabled(player, false);
                sendMessage(player, "disabled");
            }
            case "reload", "yenile" -> {
                // Yapılandırmayı yeniden yükle (yalnızca yetkili)
                if (!player.hasPermission("mossotoblok.reload")) {
                    sendUsageMessage(player);
                    return true;
                }
                plugin.getConfigManager().reloadConfig();
                sendMessage(player, "reload-success");
            }
            default -> {
                // Geçersiz argüman
                sendUsageMessage(player);
            }
        }

        return true;
    }

    /**
     * Kullanım mesajını gönder
     */
    private void sendUsageMessage(Player player) {
        String message = plugin.getConfigManager().getMessage("usage");
        player.sendMessage(ColorUtils.colorize(message));
    }

    /**
     * Config'den mesaj gönder
     */
    private void sendMessage(Player player, String key) {
        String message = plugin.getConfigManager().getMessage(key);
        player.sendMessage(ColorUtils.colorize(message));
    }
}
