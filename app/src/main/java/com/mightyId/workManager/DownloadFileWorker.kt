package com.mightyId.workManager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_FILE
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_IMAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class DownloadFileWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        var downloadId: Long? = null
    }

    private val _context = context
    override fun doWork(): Result {
        val fileType = inputData.getString("fileType")
        val fileName = inputData.getString("fileName")!!
        val url = inputData.getString("fileDownload")
        Timber.tag("DownloadFileWorker").d("doWork: fileType: $fileType")
        Timber.tag("DownloadFileWorker").d("doWork: fileName: $fileName")
        Timber.tag("DownloadFileWorker").d("doWork: url :$url")
        val dirType = when (fileType) {
            TYPE_IMAGE -> {
                Environment.DIRECTORY_PICTURES
            }
            TYPE_FILE -> {
                Environment.DIRECTORY_DOWNLOADS
            }
            else -> {
                Environment.DIRECTORY_DOWNLOADS
            }
        }
        @Suppress("DEPRECATION")
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/WorkId/" + fileName).mkdir()
        val downloadManager = _context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(dirType, "/WorkId/$fileName")
            setAllowedOverRoaming(true)
            setMimeType("*/*")
        }
        downloadId = downloadManager.enqueue(request)

        CoroutineScope(Dispatchers.IO).launch {

        }
        return Result.success()
    }

    /**
    private fun writeResponseBodyToDisk(fileName:String, body: ResponseBody): Boolean{
    val directory = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(),"/workid")
    directory.mkdirs()
    val file = File(directory,fileName)
    return try {
    val inputStream = body.byteStream()
    val fos = FileOutputStream(file)
    fos.use { output->
    val buffer = ByteArray(4 * 1024)
    var read: Int
    while (inputStream.read(buffer).also { read = it } != -1) {
    output.write(buffer, 0, read)
    }
    output.flush()
    Timber.tag("DownloadFileWorker").d("writeResponseBodyToDisk: File downloaded")
    }
    true
    }catch (e: FileNotFoundException) {
    Timber.tag("DownloadFileWorker").e("File not found")
    false
    }catch (e: IOException) {
    Timber.tag("DownloadFileWorker").e("writeResponseBodyToDisk: network error")
    false
    }catch (e: Exception){
    Timber.tag("DownloadFileWorker").e("writeResponseBodyToDisk:Unknown error $e")
    false
    }
    }*/
}