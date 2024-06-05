# gakuen-imas-localify

- 学园偶像大师 本地化插件
- **开发中**



# How to use

- 这是一个 XPosed 插件，已 Root 用户可以使用 [LSPosed](https://github.com/LSPosed/LSPosed)，未 Root 用户可以使用 [LSPatch](https://github.com/LSPosed/LSPatch)。



# TODO

- [ ] 卡片信息、TIPS 等部分的文本 hook
- [ ] TIPS 文本消失
- [ ] 更多类型的文件替换
- [ ] LSPatch 集成模式无效

... and more



# 本地化相关

- 本地化文件放在 `app/src/main/assets/gakumas-local` 内

- `version.txt` 记录翻译版本号。每次插件启动都会检查 `asset` 内的版本和本地的版本是否一致。不一致会覆盖文件到 `/data/data/com.bandainamcoent.idolmaster_gakuen/files/gakumas-lical/` 文件夹内
- `local-files/localization.json` 为 localization 翻译
- `local-files/generic.json` 为 localization 未覆盖部分的翻译
- `local-files/resource` 文件夹存放资源文件，目前可以替换所有同名的 txt 文件。获取游戏原始资源可以查看：[gkmasToolkit](https://github.com/kishidanatsumi/gkmasToolkit)



# 特别鸣谢

- [gkmasToolkit](https://github.com/kishidanatsumi/gkmasToolkit)
- [UmaPyogin-Android](https://github.com/akemimadoka/UmaPyogin-Android)
- [UnityResolve.hpp](https://github.com/issuimo/UnityResolve.hpp)
- You

