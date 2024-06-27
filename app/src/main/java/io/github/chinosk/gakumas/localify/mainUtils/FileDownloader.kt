package io.github.chinosk.gakumas.localify.mainUtils

import android.util.Log
import io.github.chinosk.gakumas.localify.TAG
import okhttp3.*
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object FileDownloader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    private var call: Call? = null

    fun downloadFile(
        url: String,
        onDownload: (Float, downloaded: Long, size: Long) -> Unit,
        onSuccess: (ByteArray) -> Unit,
        onFailed: (Int, String) -> Unit,
        checkContentTypes: List<String>? = null
    ) {
        try {
            if (call != null) {
                onFailed(-1, "Another file is downloading.")
                return
            }
            val request = Request.Builder()
                .url(url)
                .build()

            call = client.newCall(request)
            call?.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    this@FileDownloader.call = null
                    if (call.isCanceled()) {
                        onFailed(-1, "Download canceled")
                    } else {
                        onFailed(-1, e.message ?: "Unknown error")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        this@FileDownloader.call = null
                        onFailed(response.code, response.message)
                        return
                    }

                    if (checkContentTypes != null) {
                        val contentType = response.header("Content-Type")
                        if (!checkContentTypes.contains(contentType)) {
                            onFailed(-1, "Unexpected content type: $contentType")
                            this@FileDownloader.call = null
                            return
                        }
                    }

                    response.body?.let { responseBody ->
                        val contentLength = responseBody.contentLength()
                        val inputStream = responseBody.byteStream()
                        val buffer = ByteArray(8 * 1024)
                        var downloadedBytes = 0L
                        var read: Int
                        val outputStream = ByteArrayOutputStream()

                        try {
                            while (inputStream.read(buffer).also { read = it } != -1) {
                                outputStream.write(buffer, 0, read)
                                downloadedBytes += read
                                val progress = if (contentLength < 0) {
                                    0f
                                }
                                else {
                                    downloadedBytes.toFloat() / contentLength
                                }
                                onDownload(progress, downloadedBytes, contentLength)
                            }
                            onSuccess(outputStream.toByteArray())
                        } catch (e: IOException) {
                            if (call.isCanceled()) {
                                onFailed(-1, "Download canceled")
                            } else {
                                onFailed(-1, e.message ?: "Error reading stream")
                            }
                        } finally {
                            this@FileDownloader.call = null
                            inputStream.close()
                            outputStream.close()
                        }
                    } ?: run {
                        this@FileDownloader.call = null
                        onFailed(-1, "Response body is null")
                    }
                }
            })
        }
        catch (e: Exception) {
            onFailed(-1, e.toString())
            call = null
        }

    }

    fun cancel() {
        call?.cancel()
        this@FileDownloader.call = null
    }

    /**
    * return: Status, newString
    * Status: 0 - not change, 1 - need check, 2 - modified, 3 - checked
    **/
    fun checkAndChangeDownloadURL(url: String, forceEdit: Boolean = false): Pair<Int, String> {

        if (!url.startsWith("https://github.com/")) {  // check github only
            return Pair(0, url)
        }
        if (url.endsWith(".zip")) {
            return Pair(0, url)
        }

        // https://github.com/chinosk6/GakumasTranslationData
        // https://github.com/chinosk6/GakumasTranslationData.git
        // https://github.com/chinosk6/GakumasTranslationData/archive/refs/heads/main.zip
        if (url.endsWith(".git")) {
            return Pair(2, "${url.substring(0, url.length - 4)}/archive/refs/heads/main.zip")
        }

        if (forceEdit) {
            return Pair(3, "$url/archive/refs/heads/main.zip")
        }

        return Pair(1, url)
    }
}
