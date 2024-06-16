package io.github.chinosk.gakumas.localify.models


data class GakumasConfig (
    var dbgMode: Boolean = false,
    var enabled: Boolean = true,
    var replaceFont: Boolean = true,
    var textTest: Boolean = false,
    var dumpText: Boolean = false,
    var gameOrientation: Int = 0,
    var forceExportResource: Boolean = false,
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

    var enableBreastParam: Boolean = false,
    var bUseLimit: Float = 1.0f,
    var bDamping: Float = 0.33f,
    var bStiffness: Float = 0.08f,
    var bSpring: Float = 1.0f,
    var bPendulum: Float = 0.055f,
    var bPendulumRange: Float = 0.15f,
    var bAverage: Float = 0.20f,
    var bRootWeight: Float = 0.5f,
    var bUseArmCorrection: Boolean = true,
    var bUseScale: Boolean = false,
    var bScale: Float = 1.0f
)
