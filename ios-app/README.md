# Dallas Music — iOS (React Native + Expo)

نسخه آیفون دالاس موزیک با همان کاتالوگ مشترک ریپو (`/catalog.json` در ریشه)
و همان تم ابسیدین/طلایی و فونت وزیرمتن.

- Bundle ID: `com.mojtaba.raptunes` — Expo SDK 52, `expo-audio` (پخش پس‌زمینه + لاک‌اسکرین)، `expo-sqlite` (آفلاین‌اول)، `expo-file-system` (دانلود آفلاین)
- در اولین اجرا کاتالوگ باندل‌شده خودکار داخل SQLite سید می‌شود؛ بعد از آن سینک از
  `https://raw.githubusercontent.com/mojtabakhoshhal/musicdallas/main/catalog.json`
  و چک نسخه از `version.json` همان ریپو (علاقه‌مندی/پلی‌لیست/دانلودها حفظ می‌شوند)
- بیلد رایگان iOS: وورک‌فلوی `.github/workflows/ios-unsigned.yml` روی `macos-15`
  خروجی `DallasMusic.ipa` بدون امضا می‌دهد — نصب با **SideStore** و Apple ID رایگان خود کاربر
- تست‌ها: `npm test` (Jest — سید خالی، مرج ریموت با حفظ علاقه‌مندی‌ها، ساخت پلی‌لیست)

توسعه‌دهندگان: مجتبی خوشحال (برنامه‌نویسی) — امیر خوشحال (ایده و بازاریابی)
