<div align="center">

# gakuen-imas-localify-zh-TW
> 學園偶像大師 本地化插件（開發中）

[![Discord](https://dcbadge.limes.pink/api/server/https://discord.gg/gkmas)](https://discord.gg/gkmas)

</div>

# Usage

- 這是一個 XPosed 插件，已 Root 用戶可以使用 [LSPosed](https://github.com/LSPosed/LSPosed)，未 Root 用戶可以使用 [LSPatch](https://github.com/LSPosed/LSPatch)。


# Download
[下載最新版 zip](https://nightly.link/yotv2000tw/gakuen-imas-localify-zh-TW/workflows/build/main/GakumasLocalify-zh-TW.zip)

# Translate
[GakumasTranslationData_zh-TW](https://github.com/yotv2000tw/GakumasTranslationData_zh-TW)

# 本地化相關

- 本地化文件放在 `app/src/main/assets/gakumas-local` 內
- `version.txt` 記錄翻譯版本號。每次插件啟動都會檢查 `asset` 內的版本和本地的版本是否一致。不一致會覆蓋文件到 `/data/data/com.bandainamcoent.idolmaster_gakuen/files/gakumas-lical/` 文件夾內
- `local-files/localization.json` 為 localization 翻譯
- `local-files/generic.json` 為 localization 未覆蓋部分的翻譯
- `local-files/genericTrans` 文件夾內所有 `.json` 文件作用同 `generic.json`，文件夾名/檔案名可自訂，方便區分翻譯內容
- `local-files/resource` 文件夾存放資源文件，目前可以替換所有同名的 txt 文件。獲取遊戲原始資源可以查看：[gkmasToolkit](https://github.com/kishidanatsumi/gkmasToolkit)



# 特別感謝

- [gakuen-imas-localify](https://github.com/chinosk6/gakuen-imas-localify)
- [Gakumas-Localify-EN](https://github.com/NatsumeLS/Gakumas-Localify-EN)
- [gkmasToolkit](https://github.com/kishidanatsumi/gkmasToolkit)
- [UmaPyogin-Android](https://github.com/akemimadoka/UmaPyogin-Android)
- [UnityResolve.hpp](https://github.com/issuimo/UnityResolve.hpp)
- You

