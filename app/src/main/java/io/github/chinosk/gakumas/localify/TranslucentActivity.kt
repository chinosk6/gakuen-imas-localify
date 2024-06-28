package io.github.chinosk.gakumas.localify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import io.github.chinosk.gakumas.localify.GakumasHookMain.Companion.showToast
import io.github.chinosk.gakumas.localify.mainUtils.json
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfigSerializer
import kotlinx.serialization.SerializationException
import java.io.File

class TranslucentActivity : ComponentActivity() {
    private lateinit var programConfig: ProgramConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()
        val requestData = intent.getStringExtra("gkmsData")
        if (requestData != null) {
            if (requestData == "requestConfig") {
                onClickStartGame()
                finish()
            }
        }
    }

    private fun loadConfig() {
        val configStr = getConfigContent()
        try {
            json.decodeFromString<GakumasConfig>(configStr)
        } catch (e: SerializationException) {
            showToast("配置文件异常，已重置: $e")
            GakumasConfig()
        }

        val programConfigStr = getProgramConfigContent()
        programConfig = try {
            json.decodeFromString<ProgramConfig>(programConfigStr)
        }
        catch (e: SerializationException) {
            ProgramConfig()
        }
    }

    private fun onClickStartGame() {
        val intent = Intent().apply {
            setClassName("com.bandainamcoent.idolmaster_gakuen", "com.google.firebase.MessagingUnityPlayerActivity")
            putExtra("gkmsData", getConfigContent())
            putExtra("localData", getProgramConfigContent(listOf("transRemoteZipUrl", "p")))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val updateFile = File(filesDir, "update_trans.zip")
        if (updateFile.exists()) {
            val dirUri = FileProvider.getUriForFile(this, "io.github.chinosk.gakumas.localify.fileprovider", File(updateFile.absolutePath))
            intent.setDataAndType(dirUri, "resource/file")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        startActivity(intent)
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

    private fun getProgramConfigContent(excludes: List<String> = emptyList()): String {
        if (excludes.isEmpty()) {
            val configFile = File(filesDir, "localify-config.json")
            return if (configFile.exists()) {
                configFile.readText()
            }
            else {
                "{}"
            }
        }
        else {
            return json.encodeToString(ProgramConfigSerializer(excludes), programConfig)
        }
    }
}
