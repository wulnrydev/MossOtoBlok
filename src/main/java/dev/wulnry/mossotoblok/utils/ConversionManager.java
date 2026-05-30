package dev.wulnry.mossotoblok.utils;

import dev.wulnry.mossotoblok.MossOtoBlok;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Conversion Manager
 * 
 * Materyal dönüşüm işlemlerini yönetir
 * Oyuncu envanterini kontrol eder ve gerekirse dönüşüm yapar
 * 
 * Optimizasyon:
 * - Sadece event tetiklendiğinde çalışır
 * - Arka planda sürekli döngü yok
 * - Minimum işlem yükü
 * 
 * @author wulnrydev
 */
public class ConversionManager {

    private final MossOtoBlok plugin;

    public ConversionManager(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncunun envanterini kontrol et ve dönüşüm yap
     * 
     * @param player Kontrol edilecek oyuncu
     */
    public void checkAndConvertItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<Material, Material> conversions = plugin.getConfigManager().getConversions();
        int requiredAmount = plugin.getConfigManager().getRequiredAmount();

        // Her dönüşüm için kontrol et
        for (Map.Entry<Material, Material> entry : conversions.entrySet()) {
            Material from = entry.getKey();
            Material to = entry.getValue();

            // Envanterde bu materyalden kaç tane var?
            int totalAmount = countMaterial(inventory, from);

            // Yeterli materyal varsa dönüşüm yap
            if (totalAmount >= requiredAmount) {
                int conversionsToMake = totalAmount / requiredAmount;
                performConversion(inventory, from, to, conversionsToMake, requiredAmount);
            }
        }
    }

    /**
     * Envanterde belirli bir materyalden kaç tane olduğunu say
     * 
     * @param inventory Oyuncu envanteri
     * @param material Sayılacak materyal
     * @return Toplam miktar
     */
    private int countMaterial(PlayerInventory inventory, Material material) {
        int total = 0;
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        
        return total;
    }

    /**
     * Dönüşüm işlemini gerçekleştir
     * 
     * @param inventory Oyuncu envanteri
     * @param from Kaynak materyal
     * @param to Hedef materyal (blok)
     * @param conversions Kaç dönüşüm yapılacak
     * @param requiredAmount Her dönüşüm için gereken miktar
     */
    private void performConversion(PlayerInventory inventory, Material from, Material to, 
                                   int conversions, int requiredAmount) {
        
        int totalToRemove = conversions * requiredAmount;
        int removed = 0;

        // Kaynak materyali sil
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == from && removed < totalToRemove) {
                int amount = item.getAmount();
                int toRemove = Math.min(amount, totalToRemove - removed);
                
                if (toRemove >= amount) {
                    item.setType(Material.AIR);
                    item.setAmount(0);
                } else {
                    item.setAmount(amount - toRemove);
                }
                
                removed += toRemove;
            }
        }

        // Hedef materyali (blok) ekle
        ItemStack blockItem = new ItemStack(to, conversions);
        
        // Envantere ekle (yer yoksa yere düşür)
        HashMap<Integer, ItemStack> leftover = inventory.addItem(blockItem);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                inventory.getHolder().getWorld().dropItemNaturally(
                    inventory.getHolder().getLocation(), item
                );
            }
        }

        // NOT: Mesaj gönderilmiyor (chat spam'ini önlemek için)
        // Prompt'ta belirtildiği gibi: "hiçbir mesaj gönderilmeyecektir"
    }
}
