package io.github.chinosk.gakumas.localify.hookUtils

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.util.Log
import io.github.chinosk.gakumas.localify.GakumasHookMain
import io.github.chinosk.gakumas.localify.TAG
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileHotUpdater {
    private fun unzip(zipFile: InputStream, destDir: String, matchNamePrefix: String = "",
                      replaceMatchNamePrefix: String? = null) {
        val buffer = ByteArray(1024)
        try {
            val folder = File(destDir)
            if (!folder.exists()) {
                folder.mkdir()
            }

            val zipIn = ZipInputStream(zipFile)

            var entry = zipIn.nextEntry
            while (entry != null) {
                var writeEntryName = entry.name
                if (matchNamePrefix.isNotEmpty()) {
                    if (!entry.name.startsWith(matchNamePrefix)) {
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                        continue
                    }
                    replaceMatchNamePrefix?.let {
                        writeEntryName = replaceMatchNamePrefix + writeEntryName.substring(
                            matchNamePrefix.length, writeEntryName.length
                        )
                    }
                }
                val filePath = destDir + File.separator + writeEntryName
                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath, buffer)
                } else {
                    val dir = File(filePath)
                    dir.mkdirs()
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()
        } catch (e: Exception) {
            Log.e(TAG, "unzip error: $e")
        }
    }

    private fun unzip(zipFile: String, destDir: String, matchNamePrefix: String = "") {
        return unzip(FileInputStream(zipFile), destDir, matchNamePrefix)
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String, buffer: ByteArray) {
        val fout = FileOutputStream(filePath)
        var length: Int
        while (zipIn.read(buffer).also { length = it } > 0) {
            fout.write(buffer, 0, length)
        }
        fout.close()
    }

    private fun getZipResourcePath(zipFile: InputStream): String? {
        try {
            val zipIn = ZipInputStream(zipFile)

            var entry = zipIn.nextEntry
            while (entry != null) {
                if (entry.isDirectory) {
                    if (entry.name.endsWith("local-files/")) {
                        zipIn.close()
                        var retPath = File(entry.name, "..").canonicalPath
                        if (retPath.startsWith("/")) retPath = retPath.substring(1)
                        return retPath
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()
        }
        catch (e: Exception) {
            Log.e(TAG, "getZipResourcePath error: $e")
        }
        return null
    }

    private fun getZipResourceVersion(zipFile: InputStream, basePath: String): String? {
        try {
            val targetVersionFilePath = File(basePath, "version.txt").canonicalPath

            val zipIn = ZipInputStream(zipFile)
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    if ("/${entry.name}" == targetVersionFilePath) {
                        Log.d(TAG, "targetVersionFilePath: $targetVersionFilePath")
                        val reader = BufferedReader(InputStreamReader(zipIn))
                        val versionContent = reader.use { it.readText() }
                        Log.d(TAG, "versionContent: $versionContent")
                        zipIn.close()
                        return versionContent
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()
        }
        catch (e: Exception) {
            Log.e(TAG, "getZipResourceVersion error: $e")
        }
        return null
    }

    private fun getZipResourceVersion(zipFile: String, basePath: String): String? {
        return getZipResourceVersion(FileInputStream(zipFile), basePath)
    }

    fun getZipResourceVersion(zipFile: String): String? {
        return try {
            val basePath = getZipResourcePath(FileInputStream(zipFile))
            basePath?.let { getZipResourceVersion(zipFile, it) }
        }
        catch (_: Exception) {
            null
        }
    }

    fun updateFilesFromZip(activity: Activity, zipFileUri: Uri, filesDir: File, deleteAfterUpdate: Boolean) {
        try {
            GakumasHookMain.showToast("Updating files from zip...")

            var basePath: String?
            activity.contentResolver.openInputStream(zipFileUri).use {
                basePath = it?.let { getZipResourcePath(it) }
                if (basePath == null) {
                    Log.e(TAG, "getZipResourcePath failed.")
                    return@updateFilesFromZip
                }
            }

            /*
            var resourceVersion: String?
            activity.contentResolver.openInputStream(zipFileUri).use {
                resourceVersion = it?.let { getZipResourceVersion(it, basePath!!) }
                Log.d(TAG, "resourceVersion: $resourceVersion ($basePath)")
            }*/

            activity.contentResolver.openInputStream(zipFileUri).use {
                it?.let {
                    unzip(it, File(filesDir, FilesChecker.localizationFilesDir).absolutePath,
                        basePath!!, "../gakumas-local/")
                    if (deleteAfterUpdate) {
                        activity.contentResolver.delete(zipFileUri, null, null)
                    }
                    GakumasHookMain.showToast("Update success.")
                }
            }

        }
        catch (e: java.io.FileNotFoundException) {
            Log.i(TAG, "updateFilesFromZip - file not found: $e")
            GakumasHookMain.showToast("Update file not found.")
        }
        catch (e: Exception) {
            Log.e(TAG, "updateFilesFromZip failed: $e")
            GakumasHookMain.showToast("Updating files failed: $e")
        }
    }

}