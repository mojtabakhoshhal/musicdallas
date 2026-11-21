# نقشه راه کامل و پرامپت‌های آماده — Dallas Music (اندروید، iOS و آپدیت همیشگی)

این فایل برای تحویل به برنامه‌نویس یا هوش مصنوعی جهت راه‌اندازی کامل و رایگان **اندروید**، **iOS** و **سیستم آپدیت خودکار کاتالوگ و نسخه** تهیه شده است.

---

## بخش ۱: انتقال همین پروژه به گیت‌هاب و فعال‌سازی آپدیت خودکار اندروید

### گام ۱ — قرار دادن روی GitHub
1. در **Google AI Studio** از منوی تنظیمات پروژه، گزینه **Push to GitHub** را بزنید و یک مخزن جدید (مثلاً `dallas-music`) بسازید.
2. فایل‌های مهم آماده در مخزن شما:
   - `catalog.json` و `app/src/main/assets/catalog.json` (کاتالوگ ۱۲۲ آهنگ / ۲۰ هنرمند)
   - `version.json` (اعلان آخرین نسخه اندروید)
   - `.github/workflows/android-release.yml` (ساخت رایگان APK روی Ubuntu)

### گام ۲ — فعال‌سازی آپدیت آنلاین آهنگ‌ها بدون نصب مجدد APK
1. در فایل `app/src/main/java/com/example/data/RemoteSyncService.kt` (یا مستقیم از داخل تب «سازندگان ← تنظیم لینک‌های گیت‌هاب» در برنامه)، آدرس Raw مخزن خود را وارد کنید:
   - `catalogUrl`: `https://raw.githubusercontent.com/<USERNAME>/dallas-music/main/catalog.json`
   - `versionUrl`: `https://raw.githubusercontent.com/<USERNAME>/dallas-music/main/version.json`
2. از این پس، هر وقت آهنگ جدیدی به `catalog.json` روی گیت‌هاب اضافه و Commit کنید، کاربران با زدن دکمه **همگام‌سازی کاتالوگ آنلاین** (یا هنگام باز کردن بعدی برنامه) بلافاصله آهنگ‌های جدید را می‌بینند؛ بدون اینکه علاقه‌مندی‌ها یا دانلودهایشان پاک شود.

### گام ۳ — انتشار نسخه جدید اندروید (رایگان)
1. در `app/build.gradle.kts` مقدار `versionCode` را یکی بالا ببرید (مثلاً `2`) و `versionName` را به `"1.1.0"` تغییر دهید.
2. در `version.json` هم `latestVersionCode` را `2` و `latestVersionName` را `"1.1.0"` بگذارید و Push کنید.
3. از بخش **Actions** در گیت‌هاب فایل APK ساخته‌شده را دانلود کرده، با کلید دائمی خودتان (`dallas-release.keystore`) با `zipalign` و `apksigner` امضا کنید و در **GitHub Releases** آپلود کنید.

---

## بخش ۲: پرامپت آماده برای ساخت نسخه iOS (کاملاً رایگان — React Native + Expo + SideStore)

اگر می‌خواهید نسخه آیفون (`.ipa` بدون نیاز به اکانت پولی اپل، قابل نصب با SideStore) را بسازید، پرامپت زیر را دقیقاً به یک محیط کدنویسی React Native / Expo (مثل Cursor، Claude Code یا GitHub Copilot) بدهید:

```text
Build a production-ready iOS (and optional cross-platform) React Native + Expo SDK 52 music app named "Dallas Music" (bundle ID: com.mojtaba.raptunes) in Persian RTL, dark obsidian (#0B0B0F) with gold accent (#FFB800), using Vazirmatn font.

Requirements:
1. Reuse our exact catalog schema from `catalog.json`:
   [{id, artist, artist_id, title, album, source, remoteUrl, lyrics, cover, isNew}]
2. Offline-first architecture:
   - Bundle `catalog.json` inside the app and auto-seed into `expo-sqlite` on first launch with zero user action.
   - Add OTA remote sync from a configurable HTTPS `catalog.json` URL (GitHub Raw / Cloudflare Pages) that merges new tracks into SQLite while preserving user favorites, playlists, and local downloads.
   - Add `version.json` update checker that opens the latest release page in Safari.
3. Audio & offline downloads:
   - Use `expo-audio` with iOS `UIBackgroundModes: ["audio"]` and lock-screen Now Playing controls.
   - Use `expo-file-system` to download remote MP3 tracks locally and play downloaded files offline.
4. Screens (all Persian RTL):
   - Home (logo header, circular horizontal rail for all 20 artists with song counts, "تازه‌ها" cover-art shelf, "همه آهنگ‌ها" list)
   - Search (live Persian normalized search ی/ک over artists & tracks)
   - Library (Playlists create/add/play, Favorites ♥, Offline Downloads)
   - Artist Page (hero header, song count, gold "پخش تصادفی" shuffle button, track list)
   - Full Player & Mini Player Bar above bottom tabs (seek bar, shuffle, repeat, auto-advance, favorite, download, lyrics)
   - Developers screen crediting مجتبی خوشحال (برنامه‌نویسی) and امیر خوشحال (ایده و بازاریابی)
5. Free CI/CD (.github/workflows/ios-unsigned.yml):
   - Use `macos-15` free GitHub Actions runner: `npx expo prebuild --platform ios` + `xcodebuild` archive with `CODE_SIGNING_ALLOWED=NO`, package `Payload/DallasMusic.app` into unsigned `DallasMusic.ipa` artifact ready for free SideStore installation.
6. Write Jest unit tests verifying empty SQLite DB -> auto-seeding -> remote catalog merge preserving favorites -> playlist creation and track insertion.
```

---

## بخش ۳: پرامپت آماده برای ساخت نسخه وب‌اپ آیفون (PWA — بدون محدودیت ۷ روزه SideStore)

اگر می‌خواهید کاربران آیفون بدون دردسر نصب SideStore و بدون ریفرش هفتگی، از طریق Safari و گزینه **Add to Home Screen** همیشه به نسخه آنلاین دسترسی داشته باشند، این پرامپت را برای ساخت نسخه PWA روی **Cloudflare Pages** (کاملاً رایگان) استفاده کنید:

```text
Build a fast, mobile-first Persian RTL Progressive Web App (PWA) called "Dallas Music" using Vite + React + TypeScript + Tailwind CSS + IndexedDB (Dexie.js), visually matching Spotify/SoundCloud in dark obsidian (#0B0B0F) and gold (#FFB800) with Vazirmatn font.

Requirements:
1. Fetch and cache our shared `catalog.json` (exact schema: {id, artist, artist_id, title, album, source, remoteUrl, lyrics, cover, isNew}) from the same origin or configured GitHub/Cloudflare URL into IndexedDB so the app works offline immediately after first load.
2. Full iOS Safari PWA support: manifest.json (display: standalone, theme_color: #0B0B0F), Apple touch icons, Service Worker (Workbox) for offline app shell + audio caching, and Media Session API for iOS lock-screen play/pause/next/previous controls.
3. Include all screens: Home (20-artist circular rail, تازه‌ها shelf, همه آهنگ‌ها), live Persian Search, Library (Playlists, Favorites, Cached Offline tracks), Artist Page with gold Shuffle Play, Full Player + sticky Mini Player, and Developers page (مجتبی خوشحال & امیر خوشحال).
4. Include a free GitHub Actions deployment workflow to Cloudflare Pages / GitHub Pages so every push to `catalog.json` goes live globally in under 60 seconds.
```

---

## بخش ۴: معماری میزبانی رایگان و همیشه‌بالا (Zero-Cost Always-Up Stack)

| بخش | سرویس رایگان پیشنهادی | مزیت |
|---|---|---|
| سورس کد و ساخت APK/IPA | **GitHub + GitHub Actions** | رایگان برای مخازن عمومی، بیلد خودکار |
| میزبانی `catalog.json` و `version.json` | **GitHub Pages** یا **Cloudflare Pages** | CDN جهانی، بدون قطعی، آپدیت فوری با هر Commit |
| میزبانی فایل‌های صوتی مجاز و کاورها | **Cloudflare R2** | ۱۰ گیگابایت رایگان + ترافیک خروجی کاملاً رایگان (Zero Egress) |
| توزیع APK اندروید | **GitHub Releases** | لینک دانلود مستقیم، دائمی و پرسرعت |
| توزیع آیفون (رایگان) | **PWA (Cloudflare Pages)** + **SideStore (.ipa)** | دسترسی بدون نیاز به اکانت ۹۹ دلاری اپل |
