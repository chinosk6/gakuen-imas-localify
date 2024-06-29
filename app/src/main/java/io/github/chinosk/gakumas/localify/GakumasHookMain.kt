package io.github.chinosk.gakumas.localify

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chinosk.gakumas.localify.hookUtils.FilesChecker
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.system.measureTimeMillis
import io.github.chinosk.gakumas.localify.hookUtils.FileHotUpdater
import io.github.chinosk.gakumas.localify.mainUtils.json
import io.github.chinosk.gakumas.localify.models.ProgramConfig

val TAG = "GakumasLocalify"

class GakumasHookMain : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private lateinit var modulePath: String
    private var nativeLibLoadSuccess: Boolean
    private var alreadyInitialized = false
    private val targetPackageName = "com.bandainamcoent.idolmaster_gakuen"
    private val nativeLibName = "MarryKotone"

    private var gkmsDataInited = false

    private var getConfigError: Exception? = null
    private var externalFilesChecked: Boolean = false

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
//        if (lpparam.packageName == "io.github.chinosk.gakumas.localify") {
//            XposedHelpers.findAndHookMethod(
//                "io.github.chinosk.gakumas.localify.MainActivity",
//                lpparam.classLoader,
//                "showToast",
//                String::class.java,
//                object : XC_MethodHook() {
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        Log.d(TAG, "beforeHookedMethod hooked: ${param.args}")
//                    }
//                }
//            )
//        }

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

        XposedHelpers.findAndHookMethod(
            "android.app.Activity",
            lpparam.classLoader,
            "dispatchGenericMotionEvent",
            MotionEvent::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val motionEvent = param.args[0] as MotionEvent
                    val action = motionEvent.action

                    // 左摇杆的X和Y轴
                    val leftStickX = motionEvent.getAxisValue(MotionEvent.AXIS_X)
                    val leftStickY = motionEvent.getAxisValue(MotionEvent.AXIS_Y)

                    // 右摇杆的X和Y轴
                    val rightStickX = motionEvent.getAxisValue(MotionEvent.AXIS_Z)
                    val rightStickY = motionEvent.getAxisValue(MotionEvent.AXIS_RZ)

                    // 左扳机
                    val leftTrigger = motionEvent.getAxisValue(MotionEvent.AXIS_LTRIGGER)

                    // 右扳机
                    val rightTrigger = motionEvent.getAxisValue(MotionEvent.AXIS_RTRIGGER)

                    // 十字键
                    val hatX = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X)
                    val hatY = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y)

                    // 处理摇杆和扳机事件
                    joystickEvent(
                        action,
                        leftStickX,
                        leftStickY,
                        rightStickX,
                        rightStickY,
                        leftTrigger,
                        rightTrigger,
                        hatX,
                        hatY
                    )
                }
            }
        )

        val appActivityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader)
        XposedBridge.hookAllMethods(appActivityClass, "onStart", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                Log.d(TAG, "onStart")
                val currActivity = param.thisObject as Activity
                if (getConfigError != null) {
                    showGetConfigFailed(currActivity)
                }
                else {
                    initGkmsConfig(currActivity)
                }
            }
        })

        XposedBridge.hookAllMethods(appActivityClass, "onResume", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                Log.d(TAG, "onResume")
                val currActivity = param.thisObject as Activity
                if (getConfigError != null) {
                    showGetConfigFailed(currActivity)
                }
                else {
                    initGkmsConfig(currActivity)
                }
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
                        showToast("lib$nativeLibName.so loaded.")
                    }
                    else {
                        showToast("Load native library lib$nativeLibName.so failed.")
                        return
                    }

                    if (!gkmsDataInited) {
                        requestConfig(app.applicationContext)
                    }

                    FilesChecker.initDir(app.filesDir, modulePath)
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

        startLoop()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startLoop() {
        GlobalScope.launch {
            val interval = 1000L / 30
            while (isActive) {
                val timeTaken = measureTimeMillis {
                    pluginCallbackLooper()
                }
                delay(interval - timeTaken)
            }
        }
    }

    fun initGkmsConfig(activity: Activity) {
        val intent = activity.intent
        val gkmsData = intent.getStringExtra("gkmsData")
        val programData = intent.getStringExtra("localData")
        if (gkmsData != null) {
            gkmsDataInited = true
            val initConfig = try {
                json.decodeFromString<GakumasConfig>(gkmsData)
            }
            catch (e: Exception) {
                null
            }
            val programConfig = try {
                if (programData == null) {
                    ProgramConfig()
                } else {
                    json.decodeFromString<ProgramConfig>(programData)
                }
            }
            catch (e: Exception) {
                null
            }

            // 清理本地文件
            if (programConfig?.cleanLocalAssets == true) {
                FilesChecker.cleanAssets()
            }

            // 检查 files 版本和 assets 版本并更新
            if (programConfig?.checkBuiltInAssets == true) {
                FilesChecker.initAndCheck(activity.filesDir, modulePath)
            }

            // 强制导出 assets 文件
            if (initConfig?.forceExportResource == true) {
                FilesChecker.updateFiles()
            }

            // 使用热更新文件
            if (programConfig?.useRemoteAssets == true) {
                val dataUri = intent.data
                if (dataUri != null) {
                    if (!externalFilesChecked) {
                        externalFilesChecked = true
                        // Log.d(TAG, "dataUri: $dataUri")
                        FileHotUpdater.updateFilesFromZip(activity, dataUri, activity.filesDir,
                            programConfig.delRemoteAfterUpdate)
                    }
                }
            }

            loadConfig(gkmsData)
            Log.d(TAG, "gkmsData: $gkmsData")
        }
    }

    private fun showGetConfigFailedImpl(activity: Context, title: String, msg: String, infoButton: String, dlButton: String, okButton: String) {
        if (getConfigError == null) return
        val builder = AlertDialog.Builder(activity)
        val infoBuilder = AlertDialog.Builder(activity)
        val errConfigStr = getConfigError.toString()
        builder.setTitle("$title: $errConfigStr")
        getConfigError = null
        builder.setCancelable(false)
        builder.setMessage(msg)

        builder.setPositiveButton(okButton) { dialog, _ ->
            dialog.dismiss()
        }

        builder.setNegativeButton(dlButton) { dialog, _ ->
            dialog.dismiss()
            val webpage = Uri.parse("https://github.com/chinosk6/gakuen-imas-localify")
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            activity.startActivity(intent)
        }

        builder.setNeutralButton(infoButton) { _, _ ->
            infoBuilder.setTitle("Error Info")
            infoBuilder.setMessage(errConfigStr)
            val infoDialog = infoBuilder.create()
            infoDialog.show()
        }

        val dialog = builder.create()

        infoBuilder.setOnCancelListener {
            dialog.show()
        }

        dialog.show()
    }

    fun showGetConfigFailed(activity: Context) {
        val langData = when (getCurrentLanguage(activity)) {
            "zh" -> {
                mapOf(
                    "title" to "无法读取设置",
                    "message" to "配置读取失败，将使用默认配置。\n" +
                            "可能是您使用了 LSPatch 等工具的集成模式，也有可能是您拒绝了拉起插件的权限。\n" +
                            "若您使用了 LSPatch 等工具的集成模式，且没有单独安装插件本体，请下载插件本体。\n" +
                            "若您安装了插件本体，却弹出这个错误，请允许本应用拉起其他应用。",
                    "infoButton" to "详情",
                    "dlButton" to "下载",
                    "okButton" to "确定"
                )
            }
            else -> {
                mapOf(
                    "title" to "Get Config Failed",
                    "message" to "Configuration loading failed, the default configuration will be used.\n" +
                            "This might be due to the use the integration mode of LSPatch, or possibly because you denied the permission to launch the plugin.\n" +
                            "If you used the integration mode of LSPatch and did not install the plugin itself separately, please download the plugin.\n" +
                            "If you have installed the plugin but still see this error, please allow this application to launch other applications.",
                    "infoButton" to "Info",
                    "dlButton" to "Download",
                    "okButton" to "OK"
                )
            }
        }
        showGetConfigFailedImpl(activity, langData["title"]!!, langData["message"]!!, langData["infoButton"]!!,
            langData["dlButton"]!!, langData["okButton"]!!)
    }

    private fun getCurrentLanguage(context: Context): String {
        val locale: Locale = context.resources.configuration.locales.get(0)
        return locale.language
    }

    fun requestConfig(activity: Context) {
        try {
            val intent = Intent().apply {
                setClassName("io.github.chinosk.gakumas.localify", "io.github.chinosk.gakumas.localify.TranslucentActivity")
                putExtra("gkmsData", "requestConfig")
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        }
        catch (e: Exception) {
            getConfigError = e
            val fakeActivity = Activity().apply {
                intent = Intent().apply {
                    putExtra("gkmsData", "{}")
                }
            }
            initGkmsConfig(fakeActivity)
        }

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
        external fun joystickEvent(
            action: Int,
            leftStickX: Float,
            leftStickY: Float,
            rightStickX: Float,
            rightStickY: Float,
            leftTrigger: Float,
            rightTrigger: Float,
            hatX: Float,
            hatY: Float
        )
        @JvmStatic
        external fun loadConfig(configJsonStr: String)

        // Toast快速切换内容
        private var toast: Toast? = null

        @JvmStatic
        fun showToast(message: String) {
            val app = AndroidAppHelper.currentApplication()
            val context = app?.applicationContext
            if (context != null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    // 取消之前的 Toast
                    toast?.cancel()
                    // 创建新的 Toast
                    toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                    // 展示新的 Toast
                    toast?.show()
                }
            }
            else {
                Log.e(TAG, "showToast: $message failed: applicationContext is null")
            }
        }

        @JvmStatic
        external fun pluginCallbackLooper()
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