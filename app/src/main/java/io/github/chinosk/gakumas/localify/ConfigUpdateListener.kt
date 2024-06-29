package io.github.chinosk.gakumas.localify

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfigViewModel
import io.github.chinosk.gakumas.localify.models.ProgramConfigViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


interface ConfigListener {
    fun onEnabledChanged(value: Boolean)
    fun onForceExportResourceChanged(value: Boolean)
    fun onTextTestChanged(value: Boolean)
    fun onReplaceFontChanged(value: Boolean)
    fun onEnableFreeCameraChanged(value: Boolean)
    fun onTargetFpsChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onUnlockAllLiveChanged(value: Boolean)
    fun onLiveCustomeDressChanged(value: Boolean)
    fun onLiveCustomeHeadIdChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onLiveCustomeCostumeIdChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onUseCustomeGraphicSettingsChanged(value: Boolean)
    fun onRenderScaleChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onQualitySettingsLevelChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onVolumeIndexChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onMaxBufferPixelChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onChangePresetQuality(level: Int)
    fun onReflectionQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onLodQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onGameOrientationChanged(checkedId: Int)
    fun onDumpTextChanged(value: Boolean)

    fun onEnableBreastParamChanged(value: Boolean)
    fun onBDampingChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBStiffnessChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBSpringChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBPendulumChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBPendulumRangeChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBAverageChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBRootWeightChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBUseLimitChanged(value: Boolean)
    fun onBLimitXxChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBLimitXyChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBLimitYxChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBLimitYyChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBLimitZxChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBLimitZyChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBScaleChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onBUseArmCorrectionChanged(value: Boolean)
    fun onBUseScaleChanged(value: Boolean)
    fun onBClickPresetChanged(index: Int)
    fun onPCheckBuiltInAssetsChanged(value: Boolean)
    fun onPUseRemoteAssetsChanged(value: Boolean)
    fun onPCleanLocalAssetsChanged(value: Boolean)
    fun onPDelRemoteAfterUpdateChanged(value: Boolean)
    fun onPTransRemoteZipUrlChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun mainPageAssetsViewDataUpdate(downloadAbleState: Boolean? = null,
                                     downloadProgressState: Float? = null,
                                     localResourceVersionState: String? = null,
                                     errorString: String? = null)
}

class UserConfigViewModelFactory(private val initialValue: GakumasConfig) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserConfigViewModel(initialValue) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserConfigViewModel(initValue: GakumasConfig) : ViewModel() {
    val configState = MutableStateFlow(initValue)
    val config: StateFlow<GakumasConfig> = configState.asStateFlow()
}


interface ConfigUpdateListener: ConfigListener, IHasConfigItems {
    var factory: UserConfigViewModelFactory
    var viewModel: UserConfigViewModel

    var programConfigFactory: ProgramConfigViewModelFactory
    var programConfigViewModel: ProgramConfigViewModel

    fun pushKeyEvent(event: KeyEvent): Boolean
    fun checkConfigAndUpdateView() {}  // do nothing
    // fun saveConfig()
    fun saveProgramConfig()


    override fun onEnabledChanged(value: Boolean) {
        config.enabled = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 29))
    }

    override fun onForceExportResourceChanged(value: Boolean) {
        config.forceExportResource = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 30))
    }

    override fun onReplaceFontChanged(value: Boolean) {
        config.replaceFont = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 30))
    }

    override fun onTextTestChanged(value: Boolean) {
        config.textTest = value
        saveConfig()
    }

    override fun onDumpTextChanged(value: Boolean) {
        config.dumpText = value
        saveConfig()
    }

    override fun onEnableFreeCameraChanged(value: Boolean) {
        config.enableFreeCamera = value
        saveConfig()
    }

    override fun onUnlockAllLiveChanged(value: Boolean) {
        config.unlockAllLive = value
        saveConfig()
    }

    override fun onTargetFpsChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        try {
            val valueStr = s.toString()

            val value = if (valueStr == "") {
                0
            } else {
                valueStr.toInt()
            }
            config.targetFrameRate = value
            saveConfig()
        }
        catch (e: Exception) {
            return
        }
    }

    override fun onLiveCustomeDressChanged(value: Boolean) {
        config.enableLiveCustomeDress = value
        saveConfig()
    }

    override fun onLiveCustomeCostumeIdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.liveCustomeCostumeId = s.toString()
        saveConfig()
    }

    override fun onUseCustomeGraphicSettingsChanged(value: Boolean) {
        config.useCustomeGraphicSettings = value
        saveConfig()
    }

    override fun onRenderScaleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.renderScale = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0.0f
        }
        saveConfig()
    }

    override fun onQualitySettingsLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.qualitySettingsLevel = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onVolumeIndexChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.volumeIndex = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onMaxBufferPixelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.maxBufferPixel = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onLiveCustomeHeadIdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.liveCustomeHeadId = s.toString()
        saveConfig()
    }

    override fun onReflectionQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.reflectionQualityLevel = try {
            val value = s.toString().toInt()
            if (value > 5) 5 else value
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onLodQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.lodQualityLevel = try {
            val value = s.toString().toInt()
            if (value > 5) 5 else value
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onChangePresetQuality(level: Int) {
        when (level) {
            0 -> {
                config.renderScale = 0.5f
                config.qualitySettingsLevel = 1
                config.volumeIndex = 0
                config.maxBufferPixel = 1024
                config.lodQualityLevel = 1
                config.reflectionQualityLevel = 1
            }
            1 -> {
                config.renderScale = 0.59f
                config.qualitySettingsLevel = 1
                config.volumeIndex = 1
                config.maxBufferPixel = 1440
                config.lodQualityLevel = 2
                config.reflectionQualityLevel = 2
            }
            2 -> {
                config.renderScale = 0.67f
                config.qualitySettingsLevel = 2
                config.volumeIndex = 2
                config.maxBufferPixel = 2538
                config.lodQualityLevel = 3
                config.reflectionQualityLevel = 3
            }
            3 -> {
                config.renderScale = 0.77f
                config.qualitySettingsLevel = 3
                config.volumeIndex = 3
                config.maxBufferPixel = 3384
                config.lodQualityLevel = 4
                config.reflectionQualityLevel = 4
            }
            4 -> {
                config.renderScale = 1.0f
                config.qualitySettingsLevel = 5
                config.volumeIndex = 4
                config.maxBufferPixel = 8190
                config.lodQualityLevel = 5
                config.reflectionQualityLevel = 5
            }
        }
        checkConfigAndUpdateView()
        saveConfig()
    }

    override fun onGameOrientationChanged(checkedId: Int) {
        if (checkedId in listOf(0, 1, 2)) {
            config.gameOrientation = checkedId
        }
        saveConfig()
    }

    override fun onEnableBreastParamChanged(value: Boolean) {
        config.enableBreastParam = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBUseArmCorrectionChanged(value: Boolean) {
        config.bUseArmCorrection = value
        saveConfig()
    }

    override fun onBUseScaleChanged(value: Boolean) {
        config.bUseScale = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBDampingChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bDamping = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBStiffnessChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bStiffness = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBSpringChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bSpring = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBPendulumChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bPendulum = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBPendulumRangeChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bPendulumRange = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBAverageChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bAverage = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBRootWeightChanged(s: CharSequence, start: Int, before: Int, count: Int){
        config.bRootWeight = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBUseLimitChanged(value: Boolean){
        config.bUseLimit = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBLimitXxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitXx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitXyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitXy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitYxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitYx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitYyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitYy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitZxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitZx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitZyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bLimitZy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }


    override fun onBScaleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.bScale = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBClickPresetChanged(index: Int) {
        val setData: FloatArray = when (index) {
            // 0.33, 0.08, 0.7, 0.12, 0.25, 0.2, 0.8, 0, noUseArm 啥玩意
            0 -> floatArrayOf(0.33f, 0.07f, 0.7f, 0.06f, 0.25f, 0.2f, 0.5f,
                1f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f)
            1 -> floatArrayOf(0.365f, 0.06f, 0.62f, 0.07f, 0.25f, 0.2f, 0.5f,
                1f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f)
            2 -> floatArrayOf(0.4f, 0.065f, 0.55f, 0.075f, 0.25f, 0.2f, 0.5f,
                1f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f)
            3 -> floatArrayOf(0.4f, 0.065f, 0.55f, 0.075f, 0.25f, 0.2f, 0.5f,
                1f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 3.0f)
            4 -> floatArrayOf(0.4f, 0.06f, 0.4f, 0.075f, 0.55f, 0.2f, 0.8f,
                1f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 3.5f)

            5 -> floatArrayOf(0.33f, 0.08f, 0.8f, 0.12f, 0.55f, 0.2f, 1.0f,
                0f)

            else -> floatArrayOf(0.33f, 0.08f, 1.0f, 0.055f, 0.15f, 0.2f, 0.5f,
                1f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f)
        }

        config.bDamping = setData[0]
        config.bStiffness = setData[1]
        config.bSpring = setData[2]
        config.bPendulum = setData[3]
        config.bPendulumRange = setData[4]
        config.bAverage = setData[5]
        config.bRootWeight = setData[6]
        config.bUseLimit = if (setData[7] == 0f) {
            false
        }
        else {
            config.bLimitXx = setData[8]
            config.bLimitXy = setData[9]
            config.bLimitYx = setData[10]
            config.bLimitYy = setData[11]
            config.bLimitZx = setData[12]
            config.bLimitZy = setData[13]
            true
        }

        config.bUseArmCorrection = true

        checkConfigAndUpdateView()
        saveConfig()
    }

    override fun onPCheckBuiltInAssetsChanged(value: Boolean) {
        programConfig.checkBuiltInAssets = value
        saveProgramConfig()
    }

    override fun onPUseRemoteAssetsChanged(value: Boolean) {
        programConfig.useRemoteAssets = value
        saveProgramConfig()
    }

    override fun onPCleanLocalAssetsChanged(value: Boolean) {
        programConfig.cleanLocalAssets = value
        saveProgramConfig()
    }

    override fun onPDelRemoteAfterUpdateChanged(value: Boolean) {
        programConfig.delRemoteAfterUpdate = value
        saveProgramConfig()
    }

    override fun onPTransRemoteZipUrlChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        programConfig.transRemoteZipUrl = s.toString()
        saveProgramConfig()
    }

    override fun mainPageAssetsViewDataUpdate(downloadAbleState: Boolean?, downloadProgressState: Float?,
                                              localResourceVersionState: String?, errorString: String?) {
        downloadAbleState?.let { programConfigViewModel.downloadAbleState.value = downloadAbleState }
        downloadProgressState?.let{ programConfigViewModel.downloadProgressState.value = downloadProgressState }
        localResourceVersionState?.let{ programConfigViewModel.localResourceVersionState.value = localResourceVersionState }
        errorString?.let{ programConfigViewModel.errorStringState.value = errorString }
    }
}
