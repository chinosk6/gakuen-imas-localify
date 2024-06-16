package io.github.chinosk.gakumas.localify

import android.view.KeyEvent
import io.github.chinosk.gakumas.localify.databinding.ActivityMainBinding


interface ConfigListener {
    fun onClickStartGame()
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
}


interface ConfigUpdateListener: ConfigListener {
    var binding: ActivityMainBinding

    fun pushKeyEvent(event: KeyEvent): Boolean
    fun getConfigContent(): String
    fun checkConfigAndUpdateView()
    fun saveConfig()


    override fun onEnabledChanged(value: Boolean) {
        binding.config!!.enabled = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 29))
    }

    override fun onForceExportResourceChanged(value: Boolean) {
        binding.config!!.forceExportResource = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 30))
    }

    override fun onReplaceFontChanged(value: Boolean) {
        binding.config!!.replaceFont = value
        saveConfig()
        pushKeyEvent(KeyEvent(1145, 30))
    }

    override fun onTextTestChanged(value: Boolean) {
        binding.config!!.textTest = value
        saveConfig()
    }

    override fun onDumpTextChanged(value: Boolean) {
        binding.config!!.dumpText = value
        saveConfig()
    }

    override fun onEnableFreeCameraChanged(value: Boolean) {
        binding.config!!.enableFreeCamera = value
        saveConfig()
    }

    override fun onUnlockAllLiveChanged(value: Boolean) {
        binding.config!!.unlockAllLive = value
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
            binding.config!!.targetFrameRate = value
            saveConfig()
        }
        catch (e: Exception) {
            return
        }
    }

    override fun onLiveCustomeDressChanged(value: Boolean) {
        binding.config!!.enableLiveCustomeDress = value
        saveConfig()
    }

    override fun onLiveCustomeCostumeIdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.liveCustomeCostumeId = s.toString()
        saveConfig()
    }

    override fun onUseCustomeGraphicSettingsChanged(value: Boolean) {
        binding.config!!.useCustomeGraphicSettings = value
        saveConfig()
    }

    override fun onRenderScaleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.renderScale = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0.0f
        }
        saveConfig()
    }

    override fun onQualitySettingsLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.qualitySettingsLevel = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onVolumeIndexChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.volumeIndex = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onMaxBufferPixelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.maxBufferPixel = try {
            s.toString().toInt()
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onLiveCustomeHeadIdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.liveCustomeHeadId = s.toString()
        saveConfig()
    }

    override fun onReflectionQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.reflectionQualityLevel = try {
            val value = s.toString().toInt()
            if (value > 5) 5 else value
        }
        catch (e: Exception) {
            0
        }
        saveConfig()
    }

    override fun onLodQualityLevelChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.lodQualityLevel = try {
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
                binding.config!!.renderScale = 0.5f
                binding.config!!.qualitySettingsLevel = 1
                binding.config!!.volumeIndex = 0
                binding.config!!.maxBufferPixel = 1024
                binding.config!!.lodQualityLevel = 1
                binding.config!!.reflectionQualityLevel = 1
            }
            1 -> {
                binding.config!!.renderScale = 0.59f
                binding.config!!.qualitySettingsLevel = 1
                binding.config!!.volumeIndex = 1
                binding.config!!.maxBufferPixel = 1440
                binding.config!!.lodQualityLevel = 2
                binding.config!!.reflectionQualityLevel = 2
            }
            2 -> {
                binding.config!!.renderScale = 0.67f
                binding.config!!.qualitySettingsLevel = 2
                binding.config!!.volumeIndex = 2
                binding.config!!.maxBufferPixel = 2538
                binding.config!!.lodQualityLevel = 3
                binding.config!!.reflectionQualityLevel = 3
            }
            3 -> {
                binding.config!!.renderScale = 0.77f
                binding.config!!.qualitySettingsLevel = 3
                binding.config!!.volumeIndex = 3
                binding.config!!.maxBufferPixel = 3384
                binding.config!!.lodQualityLevel = 4
                binding.config!!.reflectionQualityLevel = 4
            }
            4 -> {
                binding.config!!.renderScale = 1.0f
                binding.config!!.qualitySettingsLevel = 5
                binding.config!!.volumeIndex = 4
                binding.config!!.maxBufferPixel = 8190
                binding.config!!.lodQualityLevel = 5
                binding.config!!.reflectionQualityLevel = 5
            }
        }
        checkConfigAndUpdateView()
        saveConfig()
    }

    override fun onGameOrientationChanged(checkedId: Int) {
        when (checkedId) {
            R.id.radioButtonGameDefault -> binding.config!!.gameOrientation = 0
            R.id.radioButtonGamePortrait -> binding.config!!.gameOrientation = 1
            R.id.radioButtonGameLandscape -> binding.config!!.gameOrientation = 2
        }
        saveConfig()
    }

    override fun onEnableBreastParamChanged(value: Boolean) {
        binding.config!!.enableBreastParam = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBUseArmCorrectionChanged(value: Boolean) {
        binding.config!!.bUseArmCorrection = value
        saveConfig()
    }

    override fun onBUseScaleChanged(value: Boolean) {
        binding.config!!.bUseScale = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBDampingChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bDamping = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBStiffnessChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bStiffness = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBSpringChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bSpring = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBPendulumChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bPendulum = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBPendulumRangeChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bPendulumRange = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBAverageChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bAverage = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBRootWeightChanged(s: CharSequence, start: Int, before: Int, count: Int){
        binding.config!!.bRootWeight = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBUseLimitChanged(value: Boolean){
        binding.config!!.bUseLimit = value
        saveConfig()
        checkConfigAndUpdateView()
    }

    override fun onBLimitXxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitXx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitXyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitXy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitYxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitYx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitYyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitYy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitZxChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitZx = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }

    override fun onBLimitZyChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bLimitZy = try {
            s.toString().toFloat()
        }
        catch (e: Exception) {
            0f
        }
        saveConfig()
    }


    override fun onBScaleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        binding.config!!.bScale = try {
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

        binding.config!!.bDamping = setData[0]
        binding.config!!.bStiffness = setData[1]
        binding.config!!.bSpring = setData[2]
        binding.config!!.bPendulum = setData[3]
        binding.config!!.bPendulumRange = setData[4]
        binding.config!!.bAverage = setData[5]
        binding.config!!.bRootWeight = setData[6]
        binding.config!!.bUseLimit = if (setData[7] == 0f) {
            false
        }
        else {
            binding.config!!.bLimitXx = setData[8]
            binding.config!!.bLimitXy = setData[9]
            binding.config!!.bLimitYx = setData[10]
            binding.config!!.bLimitYy = setData[11]
            binding.config!!.bLimitZx = setData[12]
            binding.config!!.bLimitZy = setData[13]
            true
        }

        binding.config!!.bUseArmCorrection = true

        checkConfigAndUpdateView()
        saveConfig()
    }

}