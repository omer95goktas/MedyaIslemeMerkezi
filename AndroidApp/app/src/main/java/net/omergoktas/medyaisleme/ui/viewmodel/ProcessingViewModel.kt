package net.omergoktas.medyaisleme.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.omergoktas.medyaisleme.data.model.ProcessingState
import net.omergoktas.medyaisleme.data.model.ToolType
import net.omergoktas.medyaisleme.data.repository.MediaProcessingRepository

class ProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaProcessingRepository(application.applicationContext)

    private val _selectedTool = MutableStateFlow(ToolType.VIDEO_TO_AUDIO)
    val selectedTool: StateFlow<ToolType> = _selectedTool.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    // Form inputs state
    val file1Uri = MutableStateFlow<Uri?>(null)
    val file2Uri = MutableStateFlow<Uri?>(null)
    val imageUri = MutableStateFlow<Uri?>(null)

    // Option selections
    val v2aFormat = MutableStateFlow("mp3")
    val a2aFormat = MutableStateFlow("mp3")
    val v2vFormat = MutableStateFlow("mp4")
    val gifStart = MutableStateFlow("0")
    val gifDuration = MutableStateFlow("5")
    val gifWidth = MutableStateFlow("480")
    val gifFps = MutableStateFlow("15")

    val a2vResolution = MutableStateFlow("1080p_horizontal")
    val i2iFormat = MutableStateFlow("jpg")
    val docFormat = MutableStateFlow("pdf")

    val createDocName = MutableStateFlow("")
    val createDocFormat = MutableStateFlow("docx")
    val createDocHtml = MutableStateFlow("")

    fun selectTool(tool: ToolType) {
        _selectedTool.value = tool
        resetForm()
    }

    fun resetForm() {
        file1Uri.value = null
        file2Uri.value = null
        imageUri.value = null
        _processingState.value = ProcessingState.Idle
    }

    fun setDirectSuccess(uri: Uri, mimeType: String) {
        _processingState.value = ProcessingState.Success("Tarama_" + System.currentTimeMillis() + ".pdf", uri)
    }

    fun startProcessing() {
        val tool = _selectedTool.value
        _processingState.value = ProcessingState.Uploading(0, 0, 0)

        viewModelScope.launch {
            when (tool) {
                ToolType.VIDEO_TO_AUDIO -> {
                    val uri = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir video dosyası seçin.")
                        return@launch
                    }
                    repository.processVideoToAudio(uri, v2aFormat.value).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.AUDIO_TO_AUDIO -> {
                    val uri = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir ses dosyası seçin.")
                        return@launch
                    }
                    repository.processAudioToAudio(uri, a2aFormat.value).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.VIDEO_TO_VIDEO -> {
                    val uri = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir video dosyası seçin.")
                        return@launch
                    }
                    val format = v2vFormat.value
                    if (format == "gif") {
                        repository.processVideoToVideo(
                            videoUri = uri,
                            format = "gif",
                            gifStart = gifStart.value,
                            gifDuration = gifDuration.value,
                            gifWidth = gifWidth.value,
                            gifFps = gifFps.value
                        ).collect { state ->
                            _processingState.value = state
                        }
                    } else {
                        repository.processVideoToVideo(uri, format).collect { state ->
                            _processingState.value = state
                        }
                    }
                }

                ToolType.AUDIO_TO_VIDEO -> {
                    val audio = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir ses dosyası seçin.")
                        return@launch
                    }
                    val img = imageUri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir kapak görseli seçin.")
                        return@launch
                    }
                    repository.processAudioToVideo(audio, img, a2vResolution.value).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.IMAGE_TO_IMAGE -> {
                    val uri = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bir resim dosyası seçin.")
                        return@launch
                    }
                    repository.processImageToImage(uri, i2iFormat.value).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.MERGE_VIDEOS -> {
                    val v1 = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 1. video dosyasını seçin.")
                        return@launch
                    }
                    val v2 = file2Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 2. video dosyasını seçin.")
                        return@launch
                    }
                    repository.processMergeVideos(v1, v2).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.MERGE_AUDIOS -> {
                    val a1 = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 1. ses dosyasını seçin.")
                        return@launch
                    }
                    val a2 = file2Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 2. ses dosyasını seçin.")
                        return@launch
                    }
                    repository.processMergeAudios(a1, a2).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.DOCUMENT_CONVERT -> {
                    val doc = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen dönüştürülecek belgeyi seçin.")
                        return@launch
                    }
                    repository.processDocumentConvert(doc, docFormat.value).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.PDF_OCR -> {
                    val pdf = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen taranmış PDF belgesini seçin.")
                        return@launch
                    }
                    repository.processPdfOcr(pdf).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.IMAGE_TO_TEXT -> {
                    val img = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen metin içeren resmi seçin.")
                        return@launch
                    }
                    repository.processImageToText(img).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.SPLIT_PDF -> {
                    val pdf = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen bölünecek PDF belgesini seçin.")
                        return@launch
                    }
                    repository.processSplitPdf(pdf).collect { state ->
                        _processingState.value = state
                    }
                }

                ToolType.MERGE_PDFS -> {
                    val p1 = file1Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 1. PDF belgesini seçin.")
                        return@launch
                    }
                    val p2 = file2Uri.value ?: run {
                        _processingState.value = ProcessingState.Error("Lütfen 2. PDF belgesini seçin.")
                        return@launch
                    }
                    repository.processMergePdfs(p1, p2).collect { state ->
                        _processingState.value = state
                    }
                }

                
                ToolType.DOCUMENT_CREATE -> {
                    val html = createDocHtml.value
                    if (html.isBlank()) {
                        _processingState.value = ProcessingState.Error("Lütfen belge için metin içeriği yazın.")
                        return@launch
                    }
                    repository.processCreateDocument(
                        docName = createDocName.value,
                        format = createDocFormat.value,
                        contentHtml = html
                    ).collect { state ->
                        _processingState.value = state
                    }
                }
            }
        }
    }
}
