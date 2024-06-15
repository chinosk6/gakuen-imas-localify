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


class MainActivity : AppCompatActivity(), ConfigUpdateListener {
    override lateinit var binding: ActivityMainBinding
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

    override fun onClickStartGame() {
        val intent = Intent().apply {
            setClassName("com.bandainamcoent.idolmaster_gakuen", "com.google.firebase.MessagingUnityPlayerActivity")
            putExtra("gkmsData", getConfigContent())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
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

    override fun getConfigContent(): String {
        val configFile = File(filesDir, "gkms-config.json")
        return if (configFile.exists()) {
            configFile.readText()
        }
        else {
            showToast("检测到第一次启动，初始化配置文件...")
            "{}"
        }
    }

    override fun saveConfig() {
        val configFile = File(filesDir, "gkms-config.json")
        configFile.writeText(Gson().toJson(binding.config!!))
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

    override fun checkConfigAndUpdateView() {
        binding.config = binding.config
        binding.notifyChange()
    }

    override fun pushKeyEvent(event: KeyEvent): Boolean {
        return dispatchKeyEvent(event)
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