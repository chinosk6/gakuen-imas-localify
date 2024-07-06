# gakuen-imas-localify

- 学园偶像大师 本地化插件
- **本仓库及其分支已被万代 [DMCA](https://github.com/github/dmca/blob/master/2024/07/2024-07-02-bandai.md)，且所有分支已被禁用**
- **This repository and its forks have been submitted a [DMCA request](https://github.com/github/dmca/blob/master/2024/07/2024-07-02-bandai.md) by Bandai Namco Entertainment Inc., and all forks have been blocked.**



- **请不要在 Issue 页发表无关信息。**若有想讨论的，请前往 [Discussions](https://github.com/chinosk6/gakuen-imas-localify/discussions) 页面讨论
- **Please do not post irrelevant information on the Issue page.** If you have anything to discuss, please go to the [Discussions](https://github.com/chinosk6/gakuen-imas-localify/discussions) page.



# Usage

- 这是一个 XPosed 插件，已 Root 用户可以使用 [LSPosed](https://github.com/LSPosed/LSPosed)，未 Root 用户可以使用 [LSPatch](https://github.com/LSPosed/LSPatch)。



# TODO

- [x] ~~卡片信息、TIPS 等部分的文本 hook (`generic`)~~
- [ ] ~~更多类型的文件替换~~

~~... and more~~ 没有 more 了



# 本地化相关

- 本地化文件放在 `app/src/main/assets/gakumas-local` 内
- `version.txt` 记录翻译版本号。每次插件启动都会检查 `asset` 内的版本和本地的版本是否一致。不一致会覆盖文件到 `/data/data/com.bandainamcoent.idolmaster_gakuen/files/gakumas-lical/` 文件夹内
- `local-files/localization.json` 为 localization 翻译
- `local-files/generic.json` 为 localization 未覆盖部分的翻译
- `local-files/genericTrans` 文件夹内所有 `.json` 文件作用同 `generic.json`，文件夹名/文件名可自定义，方便区分翻译内容
- `local-files/resource` 文件夹存放资源文件，目前可以替换所有同名的 txt 文件。获取游戏原始资源可以查看：[gkmasToolkit](https://github.com/kishidanatsumi/gkmasToolkit)



# Star History

[![Star History Chart](https://api.star-history.com/svg?repos=chinosk6/gakuen-imas-localify&type=Date)](https://star-history.com/#chinosk6/gakuen-imas-localify&Date)

