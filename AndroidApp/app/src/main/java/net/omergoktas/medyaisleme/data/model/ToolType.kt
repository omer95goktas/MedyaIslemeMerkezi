package net.omergoktas.medyaisleme.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String) {
    AUDIO("🎵 Ses İşlemleri"),
    VIDEO("🎬 Video İşlemleri"),
    DOCUMENT("📄 Belge / Doküman İşlemleri")
}

enum class ToolType(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val maxFileSizeBytes: Long = 1073741824L
) {
    VIDEO_TO_AUDIO(
        id = "panel-v2a",
        title = "Videodan Sese Dönüştür",
        description = "Yüklediğiniz videonun sesini yüksek kalitede ayıklar.",
        category = ToolCategory.AUDIO,
        icon = Icons.Default.Audiotrack
    ),
    AUDIO_TO_AUDIO(
        id = "panel-a2a",
        title = "Ses Formatı Değiştir",
        description = "Ses kayıtlarınızı istediğiniz ses formatına dönüştürün.",
        category = ToolCategory.AUDIO,
        icon = Icons.Default.Transform
    ),
    MERGE_AUDIOS(
        id = "panel-amerge",
        title = "İki Sesi Birleştir",
        description = "İki ses kaydını uç uca ekleyerek tek bir ses dosyası oluşturur.",
        category = ToolCategory.AUDIO,
        icon = Icons.Default.Audiotrack
    ),
    VIDEO_TO_VIDEO(
        id = "panel-v2v",
        title = "Video Formatı Değiştir & GIF Yap",
        description = "Videolarınızı dönüştürün veya belirlediğiniz aralıkta hareketli GIF yapın.",
        category = ToolCategory.VIDEO,
        icon = Icons.Default.Movie
    ),
    AUDIO_TO_VIDEO(
        id = "panel-a2v",
        title = "Sesten Video Oluştur",
        description = "Ses kaydınıza bir kapak görseli ekleyerek MP4 video üretin.",
        category = ToolCategory.VIDEO,
        icon = Icons.Default.VideoLibrary
    ),
    IMAGE_TO_IMAGE(
        id = "panel-i2i",
        title = "Resim Formatı Değiştir",
        description = "Görsellerinizi JPG, PNG, WEBP, ICO veya PDF formatına dönüştürün.",
        category = ToolCategory.VIDEO,
        icon = Icons.Default.Image
    ),
    MERGE_VIDEOS(
        id = "panel-vmerge",
        title = "İki Videoyu Birleştir",
        description = "İki ayrı videoyu peş peşe ekleyerek tek bir MP4 videosu yapar.",
        category = ToolCategory.VIDEO,
        icon = Icons.Default.Movie,
        maxFileSizeBytes = 2147483648L
    ),
    DOCUMENT_CONVERT(
        id = "panel-doc-convert",
        title = "Belge Dönüştürücü",
        description = "Word, PDF, Markdown, EPUB, HTML ve metin dosyalarını birbirine çevirin.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.Description
    ),
    PDF_OCR(
        id = "panel-pdf-ocr",
        title = "Taranmış PDF OCR",
        description = "Resim olarak taranmış PDF belgelerini aranabilir ve kopyalanabilir hale getirir.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.DocumentScanner
    ),
    IMAGE_TO_TEXT(
        id = "panel-img-ocr",
        title = "Resimden Yazı Okuma (OCR)",
        description = "Fotoğraflardaki ve taranmış belgelerdeki yazıları okuyup TXT olarak verir.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.PhotoLibrary
    ),
    SPLIT_PDF(
        id = "panel-split-pdf",
        title = "PDF Sayfalarını Böl",
        description = "Çok sayfalı PDF belgelerini her sayfası ayrı PDF olacak şekilde ZIP içinde verir.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.Description
    ),
    MERGE_PDFS(
        id = "panel-merge-pdf",
        title = "Birden Fazla PDF'i Birleştir",
        description = "İki ayrı PDF belgesini tek bir PDF dosyasında birleştirir.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.Description
    ),
    DOCUMENT_CREATE(
        id = "panel-doc-create",
        title = "Sıfırdan Zengin Belge Oluşturucu",
        description = "Metninizi zengin editörde yazın; Word, PDF, Markdown veya EPUB olarak kaydedin.",
        category = ToolCategory.DOCUMENT,
        icon = Icons.Default.EditNote
    )
}
