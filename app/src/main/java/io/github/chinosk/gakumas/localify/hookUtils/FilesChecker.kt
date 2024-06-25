package io.github.chinosk.gakumas.localify.hookUtils

import android.content.res.XModuleResources
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


object FilesChecker {
    lateinit var filesDir: File
    lateinit var modulePath: String
    val localizationFilesDir = "gakumas-local"
    var filesUpdated = false

    fun initAndCheck(fileDir: File, modulePath: String) {
        initDir(fileDir, modulePath)

        checkFiles()
    }

    fun initDir(fileDir: File, modulePath: String) {
        this.filesDir = fileDir
        this.modulePath = modulePath
    }

    fun checkFiles() {
        val installedVersion = getInstalledVersion()
        val pluginVersion = getPluginVersion()
        Log.d("GakumasLocal", "installedVer: $installedVersion, pluginVer: $pluginVersion")

        if (pluginVersion != installedVersion) {
            updateFiles()
        }
    }

    fun updateFiles() {
        if (filesUpdated) return
        filesUpdated = true

        Log.i("GakumasLocal", "Updating files...")
        val pluginBasePath = File(filesDir, localizationFilesDir)
        if (!pluginBasePath.exists()) {
            pluginBasePath.mkdirs()
        }

        val assets = XModuleResources.createInstance(modulePath, null).assets
        fun forAllAssetFiles(
            basePath: String,
            action: (String, InputStream?) -> Unit
        ) {
            val assetFiles = assets.list(basePath)!!
            for (file in assetFiles) {
                try {
                    assets.open("$basePath/$file")
                } catch (e: IOException) {
                    action("$basePath/$file", null)
                    forAllAssetFiles("$basePath/$file", action)
                    continue
                }.use {
                    action("$basePath/$file", it)
                }
            }
        }
        forAllAssetFiles(localizationFilesDir) { path, file ->
            val outFile = File(filesDir, path)
            if (file == null) {
                outFile.mkdirs()
            } else {
                outFile.outputStream().use { out ->
                    file.copyTo(out)
                }
            }
        }

        Log.i("GakumasLocal", "Updated")
    }

    fun getPluginVersion(): String {
        val assets = XModuleResources.createInstance(modulePath, null).assets

        for (i in assets.list(localizationFilesDir)!!) {
            if (i.toString() == "version.txt") {
                val stream = assets.open("$localizationFilesDir/$i")
                return convertToString(stream)
            }
        }
        return "0.0"
    }

    fun getInstalledVersion(): String {
        val pluginFilesDir = File(filesDir, localizationFilesDir)
        if (!pluginFilesDir.exists()) return "0.0"

        val versionFile = File(pluginFilesDir, "version.txt")
        if (!versionFile.exists()) return "0.0"
        return versionFile.readText()
    }

    fun convertToString(inputStream: InputStream?): String {
        val stringBuilder = StringBuilder()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    val success = deleteRecursively(child)
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return file.delete()
    }

    fun cleanAssets() {
        val pluginBasePath = File(filesDir, localizationFilesDir)
        val localFilesDir = File(pluginBasePath, "local-files")

        val fontFile = File(localFilesDir, "gkamsZHFontMIX.otf")
        val resourceDir = File(localFilesDir, "resource")
        val genericTransDir = File(localFilesDir, "genericTrans")
        val genericTransFile = File(localFilesDir, "generic.json")
        val i18nFile = File(localFilesDir, "localization.json")

        if (fontFile.exists()) {
            fontFile.delete()
        }
        if (deleteRecursively(resourceDir)) {
            resourceDir.mkdirs()
        }
        if (deleteRecursively(genericTransDir)) {
            genericTransDir.mkdirs()
        }
        if (genericTransFile.exists()) {
            genericTransFile.writeText("{}")
        }
        if (i18nFile.exists()) {
            i18nFile.writeText("{}")
        }
    }
}