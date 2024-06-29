package io.github.chinosk.gakumas.localify

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.chinosk.gakumas.localify.GakumasHookMain.Companion.showToast
import io.github.chinosk.gakumas.localify.mainUtils.json
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfig
import io.github.chinosk.gakumas.localify.models.ProgramConfigSerializer
import kotlinx.serialization.SerializationException
import java.io.File


interface IHasConfigItems {
    var config: GakumasConfig
    var programConfig: ProgramConfig

    fun saveConfig() {}  // do nothing
}

interface IConfigurableActivity<T : Activity> : IHasConfigItems


fun <T> T.getConfigContent(): String where T : Activity {
    val configFile = File(filesDir, "gkms-config.json")
    return if (configFile.exists()) {
        configFile.readText()
    } else {
        showToast("检测到第一次启动，初始化配置文件...")
        "{}"
    }
}

fun <T> T.getProgramConfigContent(
    excludes: List<String> = emptyList(),
    origProgramConfig: ProgramConfig? = null
): String where T : Activity {
    val configFile = File(filesDir, "localify-config.json")
    if (excludes.isEmpty()) {
        return if (configFile.exists()) {
            configFile.readText()
        } else {
            "{}"
        }
    } else {
        return if (origProgramConfig == null) {
            if (configFile.exists()) {
                val parsedConfig = json.decodeFromString<ProgramConfig>(configFile.readText())
                json.encodeToString(ProgramConfigSerializer(excludes), parsedConfig)
            } else {
                "{}"
            }
        } else {
            json.encodeToString(ProgramConfigSerializer(excludes), origProgramConfig)
        }
    }
}

fun <T> T.loadConfig() where T : Activity, T : IHasConfigItems {
    val configStr = getConfigContent()
    config = try {
        json.decodeFromString<GakumasConfig>(configStr)
    } catch (e: SerializationException) {
        showToast("配置文件异常，已重置: $e")
        GakumasConfig()
    }
    saveConfig()

    val programConfigStr = getProgramConfigContent()
    programConfig = try {
        json.decodeFromString<ProgramConfig>(programConfigStr)
    } catch (e: SerializationException) {
        ProgramConfig()
    }
}

fun <T> T.onClickStartGame() where T : Activity, T : IHasConfigItems {
    val intent = Intent().apply {
        setClassName(
            "com.bandainamcoent.idolmaster_gakuen",
            "com.google.firebase.MessagingUnityPlayerActivity"
        )
        putExtra("gkmsData", getConfigContent())
        putExtra(
            "localData",
            getProgramConfigContent(listOf("transRemoteZipUrl", "p"), programConfig)
        )
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val updateFile = File(filesDir, "update_trans.zip")
    if (updateFile.exists()) {
        val dirUri = FileProvider.getUriForFile(
            this,
            "io.github.chinosk.gakumas.localify.fileprovider",
            File(updateFile.absolutePath)
        )
        intent.setDataAndType(dirUri, "resource/file")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    startActivity(intent)
}
