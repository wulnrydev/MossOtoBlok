// src/main/java/dev/wulnry/mossotoblok/utils/ConversionManager.java
package dev.wulnry.mossotoblok.utils;

import dev.wulnry.mossotoblok.MossOtoBlok;
import dev.wulnry.mossotoblok.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conversion Manager
 *
 * Materyal dönüşüm işlemlerini yönetir.
 * Oyuncu envanterini kontrol eder ve gerekirse dönüşüm yapar.
 *
 * Optimizasyon (400+ oyunculu sunucu için):
 * <ul>
 *   <li>Sadece event tetiklendiğinde çalışır, arka plan döngüsü yoktur.</li>
 *   <li>Olayın getirdiği materyale göre hedefli kontrol (gereksiz tam tarama yok).</li>
 *   <li>Debounce + kuyruk: Oyuncu başına aynı anda yalnızca TEK kontrol görevi bulunur.
 *       Aynı tick içinde gelen onlarca event tek bir görevde birleşir.</li>
 *   <li>Sayım önbelleği (ORTA-4): Aynı tick'te aynı oyuncu/materyal yeniden sayılmaz;
 *       yalnızca {@code getStorageContents()} taranır (zırh/offhand atlanır).</li>
 * </ul>
 *
 * Thread güvenliği (KRİTİK-2):
 * <ul>
 *   <li>{@code pendingMaterials} ve {@code pendingTasks} {@link ConcurrentHashMap}'tir;
 *       async scheduler lambdaları ile ana thread arası erişim güvenlidir.</li>
 * </ul>
 *
 * Bellek güvenliği (KRİTİK-1):
 * <ul>
 *   <li>{@link #clearPending(UUID)} oyuncu ayrıldığında kuyruğu temizler ve bekleyen
 *       görevi iptal eder → heap memory leak önlenir.</li>
 * </ul>
 *
 * @author wulnrydev
 */
public class ConversionManager {

    private final MossOtoBlok plugin;

    /**
     * Oyuncu başına bekleyen (henüz işlenmemiş) aday materyaller.
     * Bir UUID'nin haritada bulunması = o oyuncu için zaten bir flush görevi
     * planlanmış demektir. Yeni eventler yeni görev açmaz, sadece bu sete eklenir.
     */
    private final ConcurrentHashMap<UUID, EnumSet<Material>> pendingMaterials = new ConcurrentHashMap<>();

    /**
     * Oyuncu başına planlanmış flush görevleri. {@link #clearPending(UUID)} ile
     * iptal edilebilmesi için referansları tutulur.
     */
    private final ConcurrentHashMap<UUID, BukkitTask> pendingTasks = new ConcurrentHashMap<>();

    /**
     * Kısa ömürlü (1 tick) sayım önbelleği (ORTA-4).
     * Aynı tick içinde aynı oyuncu için tekrarlı envanter taramasını önler.
     */
    private final ConcurrentHashMap<UUID, CountCache> countCache = new ConcurrentHashMap<>();

    /**
     * Tek bir oyuncu/tick için önbelleğe alınmış materyal sayımları.
     *
     * @param tick   Önbelleğin geçerli olduğu sunucu tick'i
     * @param counts Materyal başına sayım
     */
    private record CountCache(int tick, Map<Material, Integer> counts) {
    }

    /**
     * Tek bir flush boyunca yere düşürülen stack sayısını izleyen bağlam (ORTA-5).
     */
    private static final class DropContext {
        private int droppedStacks;
        private boolean limitHit;
    }

    /**
     * Yeni bir {@link ConversionManager} oluşturur.
     *
     * @param plugin Ana eklenti örneği
     */
    public ConversionManager(MossOtoBlok plugin) {
        this.plugin = plugin;
    }

    /**
     * Dönüşüm kontrolünü kuyruğa alır (debounce + kuyruk).
     *
     * Aynı oyuncu için bu tick içinde zaten bir görev varsa yeni görev AÇILMAZ;
     * aday materyaller mevcut sete eklenir. Böylece yüzlerce event tek görevde
     * birleşir ve oyuncu başına asla birden fazla eşzamanlı dönüşüm çalışmaz.
     *
     * @param player     İşlem yapılacak oyuncu
     * @param candidates Olayın etkilediği aday materyaller (null/boş güvenli)
     */
    public void queueConversion(Player player, Set<Material> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        UUID id = player.getUniqueId();

        // compute ile atomik birleştirme: ConcurrentHashMap üzerinde yarış koşulu olmaz.
        boolean[] scheduled = {false};
        pendingMaterials.compute(id, (key, pending) -> {
            if (pending == null) {
                EnumSet<Material> fresh = EnumSet.noneOf(Material.class);
                fresh.addAll(candidates);
                scheduled[0] = true;
                return fresh;
            }
            pending.addAll(candidates);
            return pending;
        });

        // İlk kez kuyruğa alındıysa oyuncu başına TEK görev planla.
        if (scheduled[0]) {
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> flush(id), 1L);
            pendingTasks.put(id, task);
        }
    }

    /**
     * Bir oyuncunun bekleyen tüm dönüşüm verisini temizler ve planlanmış görevini
     * iptal eder (KRİTİK-1). Oyuncu sunucudan ayrıldığında çağrılmalıdır.
     *
     * @param id Temizlenecek oyuncunun UUID'si
     */
    public void clearPending(UUID id) {
        if (id == null) {
            return;
        }
        pendingMaterials.remove(id);
        countCache.remove(id);
        BukkitTask task = pendingTasks.remove(id);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Tüm bekleyen verileri temizler ve görevleri iptal eder (eklenti kapanışı).
     */
    public void clearAll() {
        pendingMaterials.clear();
        countCache.clear();
        for (BukkitTask task : pendingTasks.values()) {
            task.cancel();
        }
        pendingTasks.clear();
    }

    /**
     * Bekleyen aday materyalleri tek seferde işler ve kuyruğu temizler.
     */
    private void flush(UUID id) {
        pendingTasks.remove(id);
        EnumSet<Material> materials = pendingMaterials.remove(id);
        if (materials == null) {
            return;
        }

        Player player = plugin.getServer().getPlayer(id);
        if (player == null || !player.isOnline()) {
            countCache.remove(id);
            return;
        }

        if (!PlayerDataManager.isAutoBlockEnabled(player)) {
            countCache.remove(id);
            return;
        }

        DropContext ctx = new DropContext();
        for (Material from : materials) {
            convert(player, from, ctx);
        }
        // İşlem bitti → bu tick'e ait sayım önbelleğini bırak.
        countCache.remove(id);
    }

    /**
     * Oyuncunun envanterindeki TÜM dönüşümleri kontrol eder.
     * Yalnızca giriş (join) taraması gibi nadir, tam tarama gereken durumlarda kullanılır.
     *
     * @param player Kontrol edilecek oyuncu
     */
    public void checkAndConvertItems(Player player) {
        Map<Material, Material> conversions = plugin.getConfigManager().getConversions();

        DropContext ctx = new DropContext();
        for (Material from : conversions.keySet()) {
            convert(player, from, ctx);
        }
        countCache.remove(player.getUniqueId());
    }

    /**
     * Tek bir materyal için dönüşüm mantığı.
     *
     * @return Envanterde değişiklik yapıldıysa true
     */
    private boolean convert(Player player, Material from, DropContext ctx) {
        if (from == null || from == Material.AIR) {
            return false;
        }

        Material to = plugin.getConfigManager().getConversions().get(from);
        if (to == null) {
            // Bu materyal bir dönüşüm kaynağı değil → envanteri hiç taramayız.
            return false;
        }

        int requiredAmount = plugin.getConfigManager().getRequiredAmount();
        if (requiredAmount <= 0) {
            return false;
        }

        int totalAmount = countMaterial(player, from);
        if (totalAmount < requiredAmount) {
            return false;
        }

        int conversions = totalAmount / requiredAmount;
        return performConversion(player, from, to, conversions, requiredAmount, ctx);
    }

    /**
     * Envanterde belirli bir materyalden kaç tane olduğunu sayar (ORTA-4).
     * Yalnızca {@code getStorageContents()} taranır (zırh/offhand atlanır) ve
     * sonuç aynı tick için önbelleğe alınır; böylece aynı tick'te aynı oyuncu için
     * tekrar tarama yapılmaz. Envanter sabit boyutlu olduğundan tarama O(1)'dir.
     *
     * @param player   Sayım yapılacak oyuncu
     * @param material Sayılacak materyal
     * @return Toplam miktar
     */
    private int countMaterial(Player player, Material material) {
        UUID id = player.getUniqueId();
        int currentTick = plugin.getServer().getCurrentTick();

        CountCache cache = countCache.get(id);
        if (cache == null || cache.tick() != currentTick) {
            cache = new CountCache(currentTick, new HashMap<>());
            countCache.put(id, cache);
        }

        Integer cached = cache.counts().get(material);
        if (cached != null) {
            return cached;
        }

        int total = 0;
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getStorageContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }

        cache.counts().put(material, total);
        return total;
    }

    /**
     * Dönüşüm işlemini garantili API ile gerçekleştirir.
     *
     * @param player         İşlem yapılacak oyuncu
     * @param from           Kaynak materyal
     * @param to             Hedef materyal (blok)
     * @param conversions    Kaç dönüşüm yapılacak
     * @param requiredAmount Her dönüşüm için gereken miktar
     * @param ctx            Flush boyunca düşürülen stack bütçesi
     * @return Değişiklik yapıldıysa true
     */
    private boolean performConversion(Player player, Material from, Material to,
                                      int conversions, int requiredAmount, DropContext ctx) {
        PlayerInventory inventory = player.getInventory();
        int totalToRemove = conversions * requiredAmount;

        // Kaynak materyali garantili şekilde sil.
        HashMap<Integer, ItemStack> notRemoved = inventory.removeItem(new ItemStack(from, totalToRemove));
        int failedToRemove = 0;
        for (ItemStack item : notRemoved.values()) {
            failedToRemove += item.getAmount();
        }

        int actuallyRemoved = totalToRemove - failedToRemove;
        // Yalnızca gerçekten silinen miktar kadar blok üret (asla dupe üretme).
        int blocksToGive = actuallyRemoved / requiredAmount;
        if (blocksToGive <= 0) {
            // Envanter bu tick içinde değişti → önbelleği geçersiz kıl.
            countCache.remove(player.getUniqueId());
            return false;
        }

        // Tam katın altında kalan, silinmiş ama bloğa dönüşmeyen artığı geri ver.
        int remainder = actuallyRemoved - (blocksToGive * requiredAmount);
        if (remainder > 0) {
            giveOrDrop(player, new ItemStack(from, remainder), ctx);
        }

        // Hedef bloğu envantere ekle (yer yoksa yere düşür).
        giveOrDrop(player, new ItemStack(to, blocksToGive), ctx);

        // Envanter değişti → bu oyuncunun sayım önbelleğini geçersiz kıl.
        countCache.remove(player.getUniqueId());
        return true;
    }

    /**
     * Eşyayı envantere eklemeye çalışır; yer yoksa oyuncunun ayağına düşürür.
     * Yere düşürme bir flush içinde {@code max-drop-stacks} ile sınırlıdır (ORTA-5):
     * limit aşılırsa fazla eşya düşürülmez ve oyuncuya bir kez uyarı gönderilir.
     */
    private void giveOrDrop(Player player, ItemStack stack, DropContext ctx) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        if (leftover.isEmpty()) {
            return;
        }

        int maxDropStacks = plugin.getConfigManager().getMaxDropStacks();

        for (ItemStack item : leftover.values()) {
            int maxStackSize = Math.max(1, item.getMaxStackSize());
            // Bu artığın kaç stack'e denk geldiğini yukarı yuvarla.
            int stacks = (item.getAmount() + maxStackSize - 1) / maxStackSize;

            if (ctx.droppedStacks + stacks > maxDropStacks) {
                // Limit aşıldı → eşyayı düşürme, yalnızca bir kez uyar.
                if (!ctx.limitHit) {
                    ctx.limitHit = true;
                    warnDropLimit(player);
                }
                continue;
            }

            ctx.droppedStacks += stacks;
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    /**
     * Drop limiti aşıldığında oyuncuya yapılandırılabilir uyarı gönderir.
     */
    private void warnDropLimit(Player player) {
        String message = plugin.getConfigManager().getMessage("drop-limit");
        if (message != null && !message.isBlank()) {
            player.sendMessage(ColorUtils.colorize(message));
        }
    }
}
