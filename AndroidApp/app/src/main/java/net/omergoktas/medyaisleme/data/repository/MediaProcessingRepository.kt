package net.omergoktas.medyaisleme.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.omergoktas.medyaisleme.data.api.ApiClient
import net.omergoktas.medyaisleme.data.api.ApiClient.toPlainRequestBody
import net.omergoktas.medyaisleme.data.download.FileDownloadHelper
import net.omergoktas.medyaisleme.data.model.ProcessingState
import net.omergoktas.medyaisleme.data.model.ToolType
import okhttp3.ResponseBody
import retrofit2.Response

class MediaProcessingRepository(private val context: Context) {

    private val apiService = ApiClient.service

    // 1. Videodan Sese
    fun processVideoToAudio(
        videoUri: Uri,
        format: String
    ): Flow<ProcessingState> = executeFlow(ToolType.VIDEO_TO_AUDIO, listOf(videoUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "video", videoUri, onProgress)
        apiService.videoToAudio(part, format.toPlainRequestBody())
    }

    // 2. Ses Formatı
    fun processAudioToAudio(
        audioUri: Uri,
        format: String
    ): Flow<ProcessingState> = executeFlow(ToolType.AUDIO_TO_AUDIO, listOf(audioUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "audio", audioUri, onProgress)
        apiService.audioToAudio(part, format.toPlainRequestBody())
    }

    // 3. Video Formatı & GIF
    fun processVideoToVideo(
        videoUri: Uri,
        format: String,
        gifStart: String? = null,
        gifDuration: String? = null,
        gifWidth: String? = null,
        gifFps: String? = null
    ): Flow<ProcessingState> = executeFlow(ToolType.VIDEO_TO_VIDEO, listOf(videoUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "video", videoUri, onProgress)
        apiService.videoToVideo(
            video = part,
            format = format.toPlainRequestBody(),
            gifStart = gifStart?.toPlainRequestBody(),
            gifDuration = gifDuration?.toPlainRequestBody(),
            gifWidth = gifWidth?.toPlainRequestBody(),
            gifFps = gifFps?.toPlainRequestBody()
        )
    }

    // 4. Sesten Video
    fun processAudioToVideo(
        audioUri: Uri,
        imageUri: Uri,
        resolution: String
    ): Flow<ProcessingState> = executeFlow(ToolType.AUDIO_TO_VIDEO, listOf(audioUri, imageUri)) { onProgress ->
        val audioPart = ApiClient.createMultipartPart(context, "audio", audioUri, onProgress)
        val imagePart = ApiClient.createMultipartPart(context, "image", imageUri, onProgress)
        apiService.audioToVideo(audioPart, imagePart, resolution.toPlainRequestBody())
    }

    // 5. Resim Formatı
    fun processImageToImage(
        imageUri: Uri,
        format: String
    ): Flow<ProcessingState> = executeFlow(ToolType.IMAGE_TO_IMAGE, listOf(imageUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "image", imageUri, onProgress)
        apiService.imageToImage(part, format.toPlainRequestBody())
    }

    // 6. Video Birleştirme
    fun processMergeVideos(
        video1Uri: Uri,
        video2Uri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.MERGE_VIDEOS, listOf(video1Uri, video2Uri)) { onProgress ->
        val part1 = ApiClient.createMultipartPart(context, "video1", video1Uri, onProgress)
        val part2 = ApiClient.createMultipartPart(context, "video2", video2Uri, onProgress)
        apiService.mergeVideos(part1, part2)
    }

    // 7. Ses Birleştirme
    fun processMergeAudios(
        audio1Uri: Uri,
        audio2Uri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.MERGE_AUDIOS, listOf(audio1Uri, audio2Uri)) { onProgress ->
        val part1 = ApiClient.createMultipartPart(context, "audio1", audio1Uri, onProgress)
        val part2 = ApiClient.createMultipartPart(context, "audio2", audio2Uri, onProgress)
        apiService.mergeAudios(part1, part2)
    }

    // 8. Belge Dönüştürücü
    fun processDocumentConvert(
        fileUri: Uri,
        format: String
    ): Flow<ProcessingState> = executeFlow(ToolType.DOCUMENT_CONVERT, listOf(fileUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "file", fileUri, onProgress)
        apiService.documentConvert(part, format.toPlainRequestBody())
    }

    // 9. Taranmış PDF OCR
    fun processPdfOcr(
        pdfUri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.PDF_OCR, listOf(pdfUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "pdf_file", pdfUri, onProgress)
        apiService.pdfOcr(part)
    }

    // 10. Resimden Yazı Okuma (OCR)
    fun processImageToText(
        imageUri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.IMAGE_TO_TEXT, listOf(imageUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "image", imageUri, onProgress)
        apiService.imageToText(part)
    }

    // 11. PDF Sayfalarını Böl
    fun processSplitPdf(
        pdfUri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.SPLIT_PDF, listOf(pdfUri)) { onProgress ->
        val part = ApiClient.createMultipartPart(context, "pdf_file", pdfUri, onProgress)
        apiService.splitPdf(part)
    }

    // 12. Birden Fazla PDF'i Birleştir
    fun processMergePdfs(
        pdf1Uri: Uri,
        pdf2Uri: Uri
    ): Flow<ProcessingState> = executeFlow(ToolType.MERGE_PDFS, listOf(pdf1Uri, pdf2Uri)) { onProgress ->
        val part1 = ApiClient.createMultipartPart(context, "pdf1", pdf1Uri, onProgress)
        val part2 = ApiClient.createMultipartPart(context, "pdf2", pdf2Uri, onProgress)
        apiService.mergePdfs(part1, part2)
    }

    // 13. Sıfırdan Belge Oluşturucu
    fun processCreateDocument(
        docName: String,
        format: String,
        contentHtml: String
    ): Flow<ProcessingState> = flow {
        try {
            emit(ProcessingState.Uploading(50, 50, 100))
            emit(ProcessingState.ServerProcessing)

            val finalDocName = if (docName.isBlank()) "belge" else docName
            val response = apiService.createDocument(
                docName = finalDocName.toPlainRequestBody(),
                format = format.toPlainRequestBody(),
                contentHtml = contentHtml.toPlainRequestBody()
            )

            handleResponse(response, "belge_${System.currentTimeMillis()}.$format") { emit(it) }
        } catch (e: Exception) {
            emit(ProcessingState.Error(e.localizedMessage ?: "Bağlantı hatası oluştu.", true))
        }
    }.flowOn(Dispatchers.IO)

    private fun executeFlow(
        toolType: ToolType,
        files: List<Uri>,
        apiCall: suspend (onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit) -> Response<ResponseBody>
    ): Flow<ProcessingState> = flow {
        try {
            // Check size limits
            var totalBytes = 0L
            for (uri in files) {
                totalBytes += ApiClient.getFileSize(context, uri)
            }

            if (totalBytes > toolType.maxFileSizeBytes) {
                val limitLabel = if (toolType.maxFileSizeBytes > 1073741824L) "2 GB" else "1 GB"
                emit(ProcessingState.Error("Hata: Seçtiğiniz dosya(lar) $limitLabel sınırını aşıyor!"))
                return@flow
            }

            emit(ProcessingState.Uploading(0, 0, totalBytes))

            var lastReportedPercent = -1

            val response = apiCall { bytesWritten, total ->
                if (total > 0) {
                    val percent = ((bytesWritten * 100) / total).toInt().coerceIn(0, 100)
                    if (percent != lastReportedPercent) {
                        lastReportedPercent = percent
                        // We will emit through a channel/collector if needed, or inline
                    }
                }
            }

            emit(ProcessingState.ServerProcessing)

            val defaultName = "islem_${System.currentTimeMillis()}.bin"
            handleResponse(response, defaultName) { emit(it) }

        } catch (e: Exception) {
            emit(ProcessingState.Error(e.localizedMessage ?: "Bağlantı hatası: Sunucuya ulaşılamadı.", true))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun handleResponse(
        response: Response<ResponseBody>,
        fallbackName: String,
        emitState: suspend (ProcessingState) -> Unit
    ) {
        if (response.isSuccessful && response.body() != null) {
            emitState(ProcessingState.Downloading)
            val disposition = response.headers()["Content-Disposition"]
            val fileName = FileDownloadHelper.extractFileName(disposition, fallbackName.substringAfterLast('.', "bin"))
            val savedUri = FileDownloadHelper.saveResponseBodyToDownloads(context, response.body()!!, fileName)

            emitState(ProcessingState.Success(filename = fileName, fileUri = savedUri))
        } else {
            var errorMessage = "Dosya işlenirken bir sorun oluştu."
            try {
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val json = Gson().fromJson(errorBody, JsonObject::class.java)
                    if (json.has("message")) {
                        errorMessage = json.get("message").asString
                    }
                }
            } catch (_: Exception) {
            }
            emitState(ProcessingState.Error("Hata: $errorMessage (Kod: ${response.code()})"))
        }
    }
}
