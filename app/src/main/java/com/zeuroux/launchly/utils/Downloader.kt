package com.zeuroux.launchly.utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class Downloader(private val client: OkHttpClient) {
    private val downloads = mutableMapOf<String, DownloadTask>()
    private val scope = CoroutineScope(Dispatchers.IO)

    sealed class DownloadStatus {
        data class OnProgress(val downloadedSize: Long, val speed: Float): DownloadStatus()
        data class OnFinish(val files: List<File>): DownloadStatus()
        data class OnFailure(val fileName: String, val failureReason: String): DownloadStatus()
        data class OnCancel(val fileName: String): DownloadStatus()
        data class OnDownloadSuccess(val fileName: String): DownloadStatus()
    }

    data class DownloadTask(
        val url: String,
        val file: File,
        var progress: Int = 0,
        var isPaused: Boolean = false,
        var isCancelled: Boolean = false,
        var downloadJob: Job? = null
    )

    fun startDownload(urlToFileNameMap: Map<String, String>, destinationDir: File, onStatus: (DownloadStatus) -> Unit) {
        destinationDir.mkdirs()
        val downloadedSizeMap = mutableMapOf<String, Long>()
        var successCount = 0
        var lastDownloadedSize = 0L
        var lastStatus = 0L
        var speed = 0f
        urlToFileNameMap.forEach { (fileName, url) ->
            val file = File(destinationDir, "$fileName.temp")
            val task = DownloadTask(url, file)
            downloadedSizeMap[fileName] = 0
            downloads[fileName] = task

            task.downloadJob = scope.launch {
                downloadFile(task) { status ->
                    when (status) {
                        is DownloadStatus.OnProgress -> {
                            downloadedSizeMap[fileName] = status.downloadedSize
                            val downloadedSize = downloadedSizeMap.values.sum()
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastStatus > 1000) {
                                speed = (downloadedSize - lastDownloadedSize) / 1024 / 1024f
                                lastStatus = currentTime
                                lastDownloadedSize = downloadedSize
                            }
                            onStatus(DownloadStatus.OnProgress(downloadedSize, speed))
                        }

                        is DownloadStatus.OnDownloadSuccess -> {
                            file.renameTo(File(destinationDir, fileName))
                            successCount++
                            onStatus(status)
                            if (successCount == urlToFileNameMap.size) {
                                onStatus(DownloadStatus.OnFinish(downloads.values.map { it.file }))
                            }
                        }

                        is DownloadStatus.OnFailure -> {
                            if (status.failureReason.contains("cancelled", true)) {
                                task.file.delete()
                                onStatus(DownloadStatus.OnCancel(status.fileName))
                            } else {
                                onStatus(status)
                                cancelAll()
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private suspend fun downloadFile(task: DownloadTask, onStatus: (DownloadStatus) -> Unit) {
        val request = Request.Builder()
            .url(task.url)
            .build()

        try {
            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    onStatus(DownloadStatus.OnFailure(task.file.name, "HTTP error: ${response.code}"))
                    return@withContext
                }

                val inputStream = response.body?.byteStream()
                val outputStream = task.file.outputStream()
                val buffer = ByteArray(100)
                var bytesRead: Int
                var totalBytesRead = 0L

                if (inputStream == null) {
                    onStatus(DownloadStatus.OnFailure(task.file.name, "Failed to open input stream"))
                    return@withContext
                }

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1 && !task.isCancelled) {
                        if (task.isPaused) {
                            delay(500)
                            continue
                        }

                        totalBytesRead += bytesRead
                        outputStream.write(buffer, 0, bytesRead)

                        onStatus(DownloadStatus.OnProgress(totalBytesRead, 0f))
                    }
                } finally {
                    inputStream.close()
                    outputStream.close()
                }
            }
            onStatus(DownloadStatus.OnDownloadSuccess(task.file.name))
        } catch (e: Exception) {
            e.printStackTrace()
            onStatus(DownloadStatus.OnFailure(task.file.name, e.message ?: "Unknown error"))
        }
    }


    fun pauseAll() {
        downloads.values.forEach { task ->
            task.isPaused = true
        }
    }

    fun resumeAll() {
        downloads.values.forEach { task ->
            task.isPaused = false
        }
    }

    fun cancelAll() {
        downloads.values.forEach { task ->
            task.isCancelled = true
            task.downloadJob?.cancel()
        }
    }
}
