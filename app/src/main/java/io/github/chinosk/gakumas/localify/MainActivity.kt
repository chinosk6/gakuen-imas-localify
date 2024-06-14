package io.github.chinosk.gakumas.localify


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.chinosk.gakumas.localify.databinding.ActivityMainBinding
import io.github.chinosk.gakumas.localify.hookUtils.FilesChecker
import io.github.chinosk.gakumas.localify.hookUtils.MainKeyEventDispatcher
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import java.io.File


interface ConfigListener {
    fun onClickStartGame()
    fun onEnabledChanged(value: Boolean)
    fun onForceExportResourceChanged(value: Boolean)
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
    fun onGameOrientationChanged(checkedId: Int)
    fun onDumpTextChanged(value: Boolean)
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
            if (requestData == "requestConfig") {
                onClickStartGame()
                finish()
            }
        }
        showVersion()

        val scrollView: ScrollView = findViewById(R.id.scrollView)
        scrollView.viewTreeObserver.addOnScrollChangedListener { onScrollChanged() }
        onScrollChanged()

        val coordinatorLayout = findViewById<View>(R.id.coordinatorLayout)
        coordinatorLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                onScrollChanged()
                coordinatorLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun onScrollChanged() {
        val fab: FloatingActionButton = findViewById(R.id.fabStartGame)
        val startGameButton: MaterialButton = findViewById(R.id.StartGameButton)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        val location = IntArray(2)
        startGameButton.getLocationOnScreen(location)
        val buttonTop = location[1]
        val buttonBottom = buttonTop + startGameButton.height

        val scrollViewLocation = IntArray(2)
        scrollView.getLocationOnScreen(scrollViewLocation)
        val scrollViewTop = scrollViewLocation[1]
        val scrollViewBottom = scrollViewTop + scrollView.height

        val isButtonVisible = buttonTop >= scrollViewTop && buttonBottom <= scrollViewBottom

        if (isButtonVisible) {
            fab.hide()
        } else {
            fab.show()
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

    @SuppressLint("SetTextI18n")
    private fun showVersion() {
        val titleLabel = findViewById<TextView>(R.id.textViewTitle)
        val versionLabel = findViewById<TextView>(R.id.textViewResVersion)
        var versionText = "unknown"

        try {
            val stream = assets.open("${FilesChecker.localizationFilesDir}/version.txt")
            versionText = FilesChecker.convertToString(stream)

            val packInfo = packageManager.getPackageInfo(packageName, 0)
            val version = packInfo.versionName
            val versionCode = packInfo.longVersionCode
            titleLabel.text = "${titleLabel.text} $version ($versionCode)"
        }
        catch (_: Exception) {}
        versionLabel.text = "Assets Version: $versionText"
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
        dispatchKeyEvent(KeyEvent(1145, 29))
    }

    override fun onForceExportResourceChanged(value: Boolean) {
        binding.config!!.forceExportResource = value
        saveConfig()
        dispatchKeyEvent(KeyEvent(1145, 30))
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

    private fun checkConfigAndUpdateView() {
        binding.config = binding.config
        binding.notifyChange()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Log.d(TAG, "${event.keyCode}, ${event.action}")
        if (MainKeyEventDispatcher.checkDbgKey(event.keyCode, event.action)) {
            val origDbg = binding.config?.dbgMode
            if (origDbg != null) {
                binding.config!!.dbgMode = !origDbg
                checkConfigAndUpdateView()
                saveConfig()
                showToast("TestMode: ${!origDbg}")
            }
        }
        return if (event.action == 1145) true else super.dispatchKeyEvent(event)
    }
}