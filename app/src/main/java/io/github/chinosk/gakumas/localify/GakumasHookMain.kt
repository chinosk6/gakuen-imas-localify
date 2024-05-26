package io.github.chinosk.gakumas.localify

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AndroidAppHelper
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chinosk.gakumas.localify.hookUtils.FilesChecker
import android.view.KeyEvent
import android.widget.Toast
import de.robv.android.xposed.XposedBridge
import java.io.File

val TAG = "GakumasLocalify"

class GakumasHookMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private lateinit var modulePath: String
    private var nativeLibLoadSuccess: Boolean
    private var alreadyInitialized = false
    private val targetPackageName = "com.bandainamcoent.idolmaster_gakuen"
    private val nativeLibName = "MarryKotone"

    private var gkmsDataInited = false

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != targetPackageName) {
            return
        }

        XposedHelpers.findAndHookMethod(
            "android.app.Activity",
            lpparam.classLoader,
            "dispatchKeyEvent",
            KeyEvent::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val keyEvent = param.args[0] as KeyEvent
                    val keyCode = keyEvent.keyCode
                    val action = keyEvent.action
                    // Log.d(TAG, "Key event: keyCode=$keyCode, action=$action")
                    keyboardEvent(keyCode, action)
                }
            }
        )

        val appActivityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)
        XposedBridge.hookAllMethods(appActivityClass, "onStart", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                Log.d(TAG, "onStart")
                val currActivity = param.thisObject as Activity
                initGkmsConfig(currActivity)
            }
        })

        XposedBridge.hookAllMethods(appActivityClass, "onResume", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                Log.d(TAG, "onResume")
                val currActivity = param.thisObject as Activity
                initGkmsConfig(currActivity)
            }
        })

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
                    if (nativeLibLoadSuccess) {
                        showToast("lib$nativeLibName.so 已加载")
                    }
                    else {
                        showToast("加载 native 库 lib$nativeLibName.so 失败")
                        return
                    }

                    if (!gkmsDataInited) {
                        requestConfig(app.applicationContext)
                    }

                    FilesChecker.initAndCheck(app.filesDir, modulePath)
                    initHook(
                        "${app.applicationInfo.nativeLibraryDir}/libil2cpp.so",
                        File(
                            app.filesDir.absolutePath,
                            FilesChecker.localizationFilesDir
                        ).absolutePath
                    )

                    alreadyInitialized = true
                }
            })
    }

    fun initGkmsConfig(activity: Activity) {
        val intent = activity.intent
        val gkmsData = intent.getStringExtra("gkmsData")
        if (gkmsData != null) {
            gkmsDataInited = true
            loadConfig(gkmsData)
            Log.d(TAG, "gkmsData: $gkmsData")
        }
    }

    fun requestConfig(activity: Context) {
        val intent = Intent().apply {
            setClassName("io.github.chinosk.gakumas.localify", "io.github.chinosk.gakumas.localify.MainActivity")
            putExtra("gkmsData", "芜湖")
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        // activity.startActivityForResult(intent, 114514)
        activity.startActivity(intent)
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    companion object {
        @JvmStatic
        external fun initHook(targetLibraryPath: String, localizationFilesDir: String)
        @JvmStatic
        external fun keyboardEvent(keyCode: Int, action: Int)
        @JvmStatic
        external fun loadConfig(configJsonStr: String)

        @JvmStatic
        fun showToast(message: String) {
            val app = AndroidAppHelper.currentApplication()
            val context = app?.applicationContext
            if (context != null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Log.e(TAG, "showToast: $message failed: applicationContext is null")
            }
        }
    }

    init {
        ShadowHook.init(
            ConfigBuilder()
                .setMode(ShadowHook.Mode.UNIQUE)
                .build()
        )

        nativeLibLoadSuccess = try {
            System.loadLibrary(nativeLibName)
            true
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }
}