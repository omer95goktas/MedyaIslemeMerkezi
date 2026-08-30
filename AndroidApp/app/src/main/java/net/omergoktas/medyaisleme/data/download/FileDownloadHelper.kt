package net.omergoktas.medyaisleme.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import net.omergoktas.medyaisleme.R
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileDownloadHelper {

    private const val CHANNEL_ID = "medya_isleme_downloads"
    private const val NOTIFICATION_ID = 1001

    fun extractFileName(contentDisposition: String?, defaultExtension: String = "bin"): String {
        if (!contentDisposition.isNullOrBlank() && contentDisposition.contains("filename=")) {
            val parts = contentDisposition.split("filename=")
            if (parts.size > 1) {
                var name = parts[1].split(";")[0].trim()
                name = name.replace("\"", "").replace("'", "")
                if (name.isNotBlank()) return name
            }
        }
        return "omer_goktas_net_${System.currentTimeMillis()}.$defaultExtension"
    }

    fun saveResponseBodyToDownloads(
        context: Context,
        responseBody: ResponseBody,
        suggestedFileName: String
    ): Uri? {
        val extension = suggestedFileName.substringAfterLast('.', "")
        val mimeType = if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                ?: "application/octet-stream"
        } else {
            "application/octet-stream"
        }

        var savedUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, suggestedFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MedyaIsleme")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    writeStream(responseBody.byteStream(), outputStream)
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                savedUri = uri
            }
        } else {
            // Legacy Storage for Android 9 and below
            val downloadsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "MedyaIsleme"
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val targetFile = File(downloadsDir, suggestedFileName)
            FileOutputStream(targetFile).use { outputStream ->
                writeStream(responseBody.byteStream(), outputStream)
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            savedUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
        }

        showDownloadCompletedNotification(context, suggestedFileName, savedUri, mimeType)
        return savedUri
    }

    private fun writeStream(inputStream: InputStream, outputStream: OutputStream) {
        inputStream.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
    }

    fun showDownloadCompletedNotification(
        context: Context,
        fileName: String,
        fileUri: Uri?,
        mimeType: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            if (fileUri != null) {
                setDataAndType(fileUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Dosya İndirildi")
            .setContentText(fileName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun openFile(context: Context, uri: Uri) {
        try {
            val mimeType = context.contentResolver.getType(uri) ?: "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic view
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }
}
