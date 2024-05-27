package io.github.chinosk.gakumas.localify.models

import androidx.databinding.BaseObservable

data class GakumasConfig (
    var enabled: Boolean = true,
    var textTest: Boolean = false,
    var enableFreeCamera: Boolean = false,
    var targetFrameRate: Int = 0,
    var unlockAllLive: Boolean = false,
    var enableLiveCustomeDress: Boolean = false,
    var liveCustomeHeadId: String = "",
    var liveCustomeCostumeId: String = "",

    var useCustomeGraphicSettings: Boolean = false,
    var renderScale: Float = 0.77f,
    var qualitySettingsLevel: Int = 3,
    var volumeIndex: Int = 3,
    var maxBufferPixel: Int = 3384,
    var reflectionQualityLevel: Int = 4,  // 0~5
    var lodQualityLevel: Int = 4,  // 0~5
)
