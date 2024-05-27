package io.github.chinosk.gakumas.localify


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.chinosk.gakumas.localify.databinding.ActivityMainBinding
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import java.io.File


interface ConfigListener {
    fun onClickStartGame()
    fun onEnabledChanged(value: Boolean)
    fun onTextTestChanged(value: Boolean)
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
}

class MainActivity : AppCompatActivity(), ConfigListener {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "GakumasLocalify"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        loadConfig()
        binding.listener = this

        val requestData = intent.getStringExtra("gkmsData")
        if (requestData != null) {
            onClickStartGame()
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getConfigContent(): String {
        val configFile = File(filesDir, "gkms-config.json")
        return if (configFile.exists()) {
            configFile.readText()
        }
        else {
            showToast("检测到第一次启动，初始化配置文件...")
            "{}"
        }
    }

    private fun loadConfig() {
        val configStr = getConfigContent()
        binding.config = try {
            Gson().fromJson(configStr, GakumasConfig::class.java)
        }
        catch (e: JsonSyntaxException) {
            showToast("配置文件异常，已重置: $e")
            Gson().fromJson("{}", GakumasConfig::class.java)
        }
        saveConfig()
    }

    private fun saveConfig() {
        val configFile = File(filesDir, "gkms-config.json")
        configFile.writeText(Gson().toJson(binding.config!!))
    }

    override fun onEnabledChanged(value: Boolean) {
        binding.config!!.enabled = value
        saveConfig()
    }

    override fun onTextTestChanged(value: Boolean) {
        binding.config!!.textTest = value
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

    override fun onClickStartGame() {
        val intent = Intent().apply {
            setClassName("com.bandainamcoent.idolmaster_gakuen", "com.google.firebase.MessagingUnityPlayerActivity")
            putExtra("gkmsData", getConfigContent())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
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
        binding.config = binding.config
        binding.notifyChange()
        saveConfig()
    }

    private fun showTextInputLayoutHint(view: TextInputLayout) {
        showToast(view.hint.toString())
    }
}