package io.github.chinosk.gakumas.localify

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.util.Log
import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chinosk.gakumas.localify.hookUtils.FilesChecker
import kotlinx.coroutines.currentCoroutineContext
import java.io.File


class GakumasHookMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private lateinit var modulePath: String
    private var alreadyInitialized = false
    private val TAG = "GakumasLocalify"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.bandainamcoent.idolmaster_gakuen") {
            return
        }

        val cls = lpparam.classLoader.loadClass("com.unity3d.player.UnityPlayer")
        XposedHelpers.findAndHookMethod(
            cls,
            "loadNative",
            String::class.java,
            object : XC_MethodHook() {
                @SuppressLint("UnsafeDynamicallyLoadedCode")
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)

                    Log.i(TAG, "UnityPlayer.loadNative")

                    if (alreadyInitialized) {
                        return
                    }

                    val app = AndroidAppHelper.currentApplication()

                    FilesChecker.initAndCheck(app.filesDir, modulePath)

                    initHook("${app.applicationInfo.nativeLibraryDir}/libil2cpp.so",
                        File(app.filesDir.absolutePath, FilesChecker.localizationFilesDir).absolutePath)

                    alreadyInitialized = true
                }
            })
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    companion object {
        @JvmStatic
        external fun initHook(targetLibraryPath: String, localizationFilesDir: String)
    }


    init {
        ShadowHook.init(
            ConfigBuilder()
                .setMode(ShadowHook.Mode.UNIQUE)
                .build()
        )
        System.loadLibrary("MarryKotone")
    }
}