package io.github.chinosk.gakumas.localify


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.github.chinosk.gakumas.localify.databinding.ActivityMainBinding
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import java.io.File


interface ConfigListener {
    fun onClickStartGame()
    fun onEnabledChanged(value: Boolean)
    fun onEnableFreeCameraChanged(value: Boolean)
    fun onTargetFpsChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onUnlockAllLiveChanged(value: Boolean)
    fun onLiveCustomeDressChanged(value: Boolean)
    fun onLiveCustomeHeadIdChanged(s: CharSequence, start: Int, before: Int, count: Int)
    fun onLiveCustomeCostumeIdChanged(s: CharSequence, start: Int, before: Int, count: Int)
}

class MainActivity : AppCompatActivity(), ConfigListener {
    private lateinit var config: GakumasConfig
    private val TAG = "GakumasLocalify"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadConfig()

        val requestData = intent.getStringExtra("gkmsData")
        if (requestData != null) {
            onClickStartGame()
            finish()
        }

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.config = config
        binding.listener = this
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
        val config = try {
            Gson().fromJson(configStr, GakumasConfig::class.java)
        }
        catch (e: JsonSyntaxException) {
            showToast("配置文件异常，已重置: $e")
            Gson().fromJson("{}", GakumasConfig::class.java)
        }
        this.config = config
        saveConfig()
    }

    private fun saveConfig() {
        val configFile = File(filesDir, "gkms-config.json")
        configFile.writeText(Gson().toJson(config))
    }

    override fun onEnabledChanged(value: Boolean) {
        config.enabled = value
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

    override fun onLiveCustomeHeadIdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        config.liveCustomeHeadId = s.toString()
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
}