package io.github.chinosk.gakumas.localify.models

data class GakumasConfig(
    var enabled: Boolean = true,
    var enableFreeCamera: Boolean = false,
    var targetFrameRate: Int = 0
)

