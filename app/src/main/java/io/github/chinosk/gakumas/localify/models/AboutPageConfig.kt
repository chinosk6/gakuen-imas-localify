package io.github.chinosk.gakumas.localify.models

data class AboutPageConfig (
    var plugin_repo: String = "https://github.com/chinosk6/gakuen-imas-localify",
    var main_contributors: List<MainContributors> = listOf(),
    var contrib_img: ContribImg = ContribImg(
        "https://contrib.rocks/image?repo=chinosk6/gakuen-imas-localify",
        "https://contrib.rocks/image?repo=chinosk6/GakumasTranslationData")
)

data class MainContributors (
    var name: String,
    var links: List<Links>
)

data class ContribImg (
    var plugin: String,
    var translation: String
)

data class Links (
    var name: String,
    var link: String
)