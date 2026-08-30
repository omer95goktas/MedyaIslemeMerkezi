package net.omergoktas.medyaisleme.data.api

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream

class ProgressRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val contentType: String?,
    private val onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit
) : RequestBody() {

    private val fileLength: Long by lazy {
        context.contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        } ?: -1L
    }

    override fun contentType(): MediaType? {
        return (contentType ?: "application/octet-stream").toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return fileLength
    }

    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            return
        }

        inputStream.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var uploaded: Long = 0
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                if (totalLength > 0) {
                    onProgress(uploaded, totalLength)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1048576
    }
}
