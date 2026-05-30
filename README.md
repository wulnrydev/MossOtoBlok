# MossOtoBlok

**Geliştirici:** wulnrydev  
**Sürüm:** 1.0.0  
**Minecraft Sürümü:** 1.21+  
**Java Sürümü:** 21

## 📋 Açıklama

MossOtoBlok, Minecraft sunucularınız için optimize edilmiş, sıfır lag ile çalışan otomatik blok yapma eklentisidir. Oyuncuların envanterinde belirli materyallerden 9 adet toplandığında, bu materyaller otomatik olarak blok formuna dönüştürülür.

## ✨ Özellikler

- ⚡ **Sıfır Lag:** Event-driven mimari, arka planda sürekli döngü yok
- 🎯 **Maksimum Optimizasyon:** Sadece gerekli anlarda çalışır
- 🔧 **Tamamen Yapılandırılabilir:** Sınırsız materyal ekleyebilirsiniz
- 💾 **Hafif Veri Saklama:** PersistentDataContainer ile veritabanı gerektirmez
- 🎨 **HEX Renk Desteği:** Modern mesaj tasarımı
- 🚫 **Spam Yok:** Dönüşüm sırasında mesaj gönderilmez
- 🔓 **Yetki Gerektirmez:** Herkes kullanabilir

## 📦 Kurulum

1. `MossOtoBlok.jar` dosyasını sunucunuzun `plugins` klasörüne atın
2. Sunucuyu başlatın veya yeniden başlatın
3. `plugins/MossOtoBlok/config.yml` dosyasından ayarları yapılandırın

## 🎮 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/otoblok aç` | Oto-blok sistemini açar | Yok |
| `/otoblok kapat` | Oto-blok sistemini kapatır | Yok |

**Alternatif Komutlar:** `/ob`, `/autoblok`

## ⚙️ Yapılandırma

### config.yml

```yaml
# Materyal Dönüşümleri
conversions:
  COAL: COAL_BLOCK
  DIAMOND: DIAMOND_BLOCK
  EMERALD: EMERALD_BLOCK
  IRON_INGOT: IRON_BLOCK
  GOLD_INGOT: GOLD_BLOCK
  # ... daha fazla ekleyebilirsiniz

# Gerekli Materyal Sayısı
required-amount: 9

# Mesajlar
messages:
  prefix: "&#15FF08&lBoxPvP &8▸ "
  enabled: "&7Oto-blok sistemi &#00D420başarıyla açıldı&7!"
  disabled: "&7Oto-blok sistemi &#ffd500kapatıldı&7!"
```

## 🔨 Derleme

```bash
mvn clean package
```

Derlenmiş JAR dosyası `target/MossOtoBlok-1.0.0.jar` konumunda oluşacaktır.

## 🏗️ Proje Yapısı

```
MossOtoBlok/
├── src/main/java/dev/wulnry/mossotoblok/
│   ├── MossOtoBlok.java              # Ana eklenti sınıfı
│   ├── commands/
│   │   └── OtoBlokCommand.java       # /otoblok komutu
│   ├── listeners/
│   │   ├── ItemPickupListener.java   # Eşya toplama eventi
│   │   ├── InventoryListener.java    # Envanter eventi
│   │   └── PlayerJoinListener.java   # Oyuncu giriş eventi
│   └── utils/
│       ├── ColorUtils.java           # Renk işleme
│       ├── ConfigManager.java        # Yapılandırma yönetimi
│       ├── ConversionManager.java    # Dönüşüm işlemleri
│       └── PlayerDataManager.java    # Oyuncu veri yönetimi (PDC)
├── src/main/resources/
│   ├── plugin.yml                    # Eklenti tanımı
│   └── config.yml                    # Yapılandırma dosyası
└── pom.xml                           # Maven yapılandırması
```

## 🎯 Teknik Detaylar

### Event-Driven Mimari
- `EntityPickupItemEvent`: Oyuncu eşya topladığında
- `InventoryClickEvent`: Oyuncu envanterde eşya taşıdığında
- `InventoryDragEvent`: Oyuncu envanterde eşya sürüklediğinde
- `PlayerJoinEvent`: Oyuncu sunucuya katıldığında

### PersistentDataContainer (PDC)
Oyuncu verilerini (oto-blok açık/kapalı) doğrudan oyuncunun içinde saklar:
- Veritabanı gerektirmez
- YML dosyası gerektirmez
- Performanslı ve hafif
- Oyuncu ile birlikte taşınır

### Optimizasyon
- ❌ Runnable/Timer kullanılmaz
- ❌ Sürekli döngü yok
- ✅ Sadece event anında çalışır
- ✅ Minimum işlem yükü
- ✅ Clean Code prensipleri

## 📝 Lisans

Bu proje özel bir projedir. Tüm hakları saklıdır.

## 👤 İletişim

**Geliştirici:** wulnrydev

---

**Not:** Bu eklenti Paper 1.21+ için optimize edilmiştir. Spigot üzerinde de çalışır ancak Paper kullanımı önerilir.
