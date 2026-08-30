package net.omergoktas.medyaisleme.ui.screens

import net.omergoktas.medyaisleme.data.model.ProcessingState

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

import net.omergoktas.medyaisleme.R

import net.omergoktas.medyaisleme.data.model.ToolCategory
import net.omergoktas.medyaisleme.data.model.ToolType
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import net.omergoktas.medyaisleme.ui.components.AccessibleDropdown
import net.omergoktas.medyaisleme.ui.components.DropdownOption
import net.omergoktas.medyaisleme.ui.components.FilePickerField
import net.omergoktas.medyaisleme.ui.components.ProgressStatusCard
import net.omergoktas.medyaisleme.ui.components.SloganBanner
import net.omergoktas.medyaisleme.ui.viewmodel.ProcessingViewModel

@Composable
fun MediaToolsScreen(
    viewModel: ProcessingViewModel,
    toolCategory: ToolCategory,
    onNavigateToFeedback: ((tool: String, errorType: String, sourceFormat: String, targetFormat: String) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedTool by viewModel.selectedTool.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

    val file1Uri by viewModel.file1Uri.collectAsState()
    val file2Uri by viewModel.file2Uri.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()

    val v2aFormat by viewModel.v2aFormat.collectAsState()
    val a2aFormat by viewModel.a2aFormat.collectAsState()
    val v2vFormat by viewModel.v2vFormat.collectAsState()
    val gifStart by viewModel.gifStart.collectAsState()
    val gifDuration by viewModel.gifDuration.collectAsState()
    val gifWidth by viewModel.gifWidth.collectAsState()
    val gifFps by viewModel.gifFps.collectAsState()
    val a2vResolution by viewModel.a2vResolution.collectAsState()
    val i2iFormat by viewModel.i2iFormat.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pdf?.let { pdf ->
                // Simulate success in ViewModel
                
                
                viewModel.setDirectSuccess(pdf.uri, "application/pdf")
            }
        }
    }

    val mediaTools = ToolType.entries.filter { it.category == toolCategory }
    val scrollState = rememberScrollState()

    LaunchedEffect(toolCategory) {
        if (selectedTool.category != toolCategory) {
            mediaTools.firstOrNull()?.let { viewModel.selectTool(it) }
        }
    }

    BackHandler(enabled = onNavigateBack != null) {
        onNavigateBack?.invoke()
    }

    val isProcessing = processingState is ProcessingState.Uploading ||
            processingState is ProcessingState.ServerProcessing ||
            processingState is ProcessingState.Downloading

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Slogan Banner
        SloganBanner()

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Selector Dropdown
        val toolOptions = mediaTools.map { tool ->
            DropdownOption(
                value = tool.id,
                title = tool.title,
                subtitle = tool.description
            )
        }

        AccessibleDropdown(
            label = "🎬 Kullanmak İstediğiniz Medya Aracını Seçin:",
            options = toolOptions,
            selectedValue = selectedTool.id,
            onValueSelected = { toolId ->
                mediaTools.find { it.id == toolId }?.let { viewModel.selectTool(it) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active Tool Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedTool.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = selectedTool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Tool-Specific Forms
                when (selectedTool) {
                    ToolType.VIDEO_TO_AUDIO -> {
                        FilePickerField(
                            label = "Video Dosyası Seçin:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Çıkış Ses Formatı:",
                            options = listOf(
                                DropdownOption("mp3", "MP3", "En popüler ve evrensel ses formatı (320 kbps)"),
                                DropdownOption("wav", "WAV", "Kayıpsız stüdyo ve ham ses kalitesi"),
                                DropdownOption("aac", "AAC", "Yüksek verimli gelişmiş ses kodlama"),
                                DropdownOption("m4a", "M4A", "Apple ve modern cihazlar için optimize"),
                                DropdownOption("m4r", "M4R (iPhone Zil Sesi)", "Apple iPhone özel zil sesi formatı"),
                                DropdownOption("flac", "FLAC", "Kayıpsız sıkıştırılmış yüksek kalite"),
                                DropdownOption("alac", "ALAC", "Apple Lossless kayıpsız ses formatı"),
                                DropdownOption("aiff", "AIFF", "Apple stüdyo ve kayıpsız ham ses"),
                                DropdownOption("opus", "OPUS", "Yüksek kaliteli ve düşük gecikmeli"),
                                DropdownOption("ogg", "OGG", "Açık kaynak Vorbis ses formatı"),
                                DropdownOption("ac3", "AC3 (Dolby Digital)", "Çok kanallı sinema ses formatı"),
                                DropdownOption("wma", "WMA", "Windows Media Audio formatı"),
                                DropdownOption("mp2", "MP2", "MPEG Audio Layer II yayın formatı")
                            ),
                            selectedValue = v2aFormat,
                            onValueSelected = { viewModel.v2aFormat.value = it }
                        )
                    }

                    ToolType.AUDIO_TO_AUDIO -> {
                        FilePickerField(
                            label = "Ses Dosyası Seçin:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Dönüştürülecek Format:",
                            options = listOf(
                                DropdownOption("mp3", "MP3", "En popüler ve evrensel ses formatı (320 kbps)"),
                                DropdownOption("wav", "WAV", "Kayıpsız stüdyo ve ham ses kalitesi"),
                                DropdownOption("aac", "AAC", "Yüksek verimli gelişmiş ses kodlama"),
                                DropdownOption("m4a", "M4A", "Apple ve modern cihazlar için optimize"),
                                DropdownOption("m4r", "M4R (iPhone Zil Sesi)", "Apple iPhone özel zil sesi formatı"),
                                DropdownOption("flac", "FLAC", "Kayıpsız sıkıştırılmış yüksek kalite"),
                                DropdownOption("alac", "ALAC", "Apple Lossless kayıpsız ses formatı"),
                                DropdownOption("aiff", "AIFF", "Apple stüdyo ve kayıpsız ham ses"),
                                DropdownOption("opus", "OPUS", "Yüksek kaliteli ve düşük gecikmeli"),
                                DropdownOption("ogg", "OGG", "Açık kaynak Vorbis ses formatı"),
                                DropdownOption("ac3", "AC3 (Dolby Digital)", "Çok kanallı sinema ses formatı"),
                                DropdownOption("wma", "WMA", "Windows Media Audio formatı"),
                                DropdownOption("mp2", "MP2", "MPEG Audio Layer II yayın formatı")
                            ),
                            selectedValue = a2aFormat,
                            onValueSelected = { viewModel.a2aFormat.value = it }
                        )
                    }

                    ToolType.VIDEO_TO_VIDEO -> {
                        FilePickerField(
                            label = "Video Dosyası Seçin:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Dönüştürülecek Video Formatı:",
                            options = listOf(
                                DropdownOption("mp4", "MP4 (Evrensel Format)"),
                                DropdownOption("gif", "GIF Animasyon (Gelişmiş Çözünürlük ve FPS)"),
                                DropdownOption("mkv", "MKV Formatı"),
                                DropdownOption("webm", "WEBM Formatı"),
                                DropdownOption("avi", "AVI Formatı")
                            ),
                            selectedValue = v2vFormat,
                            onValueSelected = { viewModel.v2vFormat.value = it }
                        )

                        // Advanced GIF Options Box
                        AnimatedVisibility(visible = v2vFormat == "gif") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "⚙️ Profesyonel GIF Ayarları",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = gifStart,
                                        onValueChange = { viewModel.gifStart.value = it },
                                        label = { Text("Başlangıç Saniyesi") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = gifDuration,
                                        onValueChange = { viewModel.gifDuration.value = it },
                                        label = { Text("Süre (Sn - Max 25)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                AccessibleDropdown(
                                    label = "GIF Çözünürlüğü / Boyut:",
                                    options = listOf(
                                        DropdownOption("480", "480p (Önerilen - Dengeli)"),
                                        DropdownOption("640", "640p (Geniş Ekran - Yüksek Kalite)"),
                                        DropdownOption("720", "720p (Ultra HD - Maksimum Netlik)"),
                                        DropdownOption("320", "320p (Düşük Boyut - Hızlı)"),
                                        DropdownOption("240", "240p (Mini Boyut - Forum / Avatar)")
                                    ),
                                    selectedValue = gifWidth,
                                    onValueSelected = { viewModel.gifWidth.value = it }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                AccessibleDropdown(
                                    label = "Kare Hızı (FPS / Akıcılık):",
                                    options = listOf(
                                        DropdownOption("15", "15 FPS (Önerilen - Akıcı & Optimize)"),
                                        DropdownOption("24", "24 FPS (Sinematik - Çok Akıcı)"),
                                        DropdownOption("30", "30 FPS (Ultra Akıcı)"),
                                        DropdownOption("10", "10 FPS (Ekonomik - Düşük Boyut)")
                                    ),
                                    selectedValue = gifFps,
                                    onValueSelected = { viewModel.gifFps.value = it }
                                )
                            }
                        }
                    }

                    ToolType.AUDIO_TO_VIDEO -> {
                        FilePickerField(
                            label = "Ses Dosyası Seçin:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "Kapak Resmi Seçin:",
                            mimeType = "image/*",
                            selectedUri = imageUri,
                            onUriSelected = { viewModel.imageUri.value = it },
                            maxSizeLabel = "JPG / PNG / WEBP Görsel"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "3. Video Çözünürlüğü & En-Boy Oranı:",
                            options = listOf(
                                DropdownOption("1080p_horizontal", "⭐ 1080p Full HD Yatay (1920x1080 - Varsayılan / YouTube / PC)"),
                                DropdownOption("720p_horizontal", "📺 720p HD Yatay (1280x720 - Standart Web / Hızlı İşleme)"),
                                DropdownOption("480p_horizontal", "📱 480p SD Yatay (854x480 - Küçük Dosya Boyutu)"),
                                DropdownOption("1440p_horizontal", "💎 2K QHD Yatay (2560x1440 - Yüksek Kalite)"),
                                DropdownOption("4k_horizontal", "👑 4K Ultra HD Yatay (3840x2160 - Maksimum Kalite)"),
                                DropdownOption("1080p_vertical", "📲 1080p Full HD Dikey (1080x1920 - Reels / Shorts / TikTok / Hikaye)"),
                                DropdownOption("720p_vertical", "📱 720p HD Dikey (720x1280 - Hızlı Dikey)"),
                                DropdownOption("480p_vertical", "📦 480p SD Dikey (480x854 - Düşük Boyut Dikey)"),
                                DropdownOption("1440p_vertical", "✨ 2K QHD Dikey (1440x2560 - Net Dikey Video)"),
                                DropdownOption("4k_vertical", "🌟 4K Ultra HD Dikey (2160x3840 - Ultra Dikey)")
                            ),
                            selectedValue = a2vResolution,
                            onValueSelected = { viewModel.a2vResolution.value = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ℹ️ Hiçbir ayar değiştirilmezse video otomatik olarak 1080p Full HD (1920x1080) kalitesinde üretilir. Yüklediğiniz görsel seçtiğiniz formata tam ekran olarak uyarlanacaktır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    ToolType.IMAGE_TO_IMAGE -> {
                        FilePickerField(
                            label = "Resim Dosyası Seçin:",
                            mimeType = "image/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Dönüştürülecek Resim Formatı:",
                            options = listOf(
                                DropdownOption("jpg", "JPG / JPEG"),
                                DropdownOption("png", "PNG (Şeffaf / Kayıpsız)"),
                                DropdownOption("webp", "WEBP (Modern Web Standardı)"),
                                DropdownOption("pdf", "PDF Belgesi"),
                                DropdownOption("ico", "ICO (Favicon)")
                            ),
                            selectedValue = i2iFormat,
                            onValueSelected = { viewModel.i2iFormat.value = it }
                        )
                    }

                    ToolType.MERGE_VIDEOS -> {
                        FilePickerField(
                            label = "1. Video Dosyası:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "1. Video"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "2. Video Dosyası:",
                            mimeType = "video/*",
                            selectedUri = file2Uri,
                            onUriSelected = { viewModel.file2Uri.value = it },
                            maxSizeLabel = "Maksimum toplam boyut: 2 GB"
                        )
                    }

                    ToolType.MERGE_AUDIOS -> {
                        FilePickerField(
                            label = "1. Ses Dosyası:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "1. Ses"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "2. Ses Dosyası:",
                            mimeType = "audio/*",
                            selectedUri = file2Uri,
                            onUriSelected = { viewModel.file2Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (selectedTool == ToolType.DOCUMENT_SCANNER) {
                            val options = GmsDocumentScannerOptions.Builder()
                                .setGalleryImportAllowed(true)
                                .setPageLimit(25)
                                .setResultFormats(RESULT_FORMAT_PDF)
                                .setScannerMode(SCANNER_MODE_FULL)
                                .build()
                            GmsDocumentScanning.getClient(options)
                                .getStartScanIntent(activity!!)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                }
                        } else {
                            viewModel.startProcessing()
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .accessibleTouchTarget()
                        .semantics {
                            contentDescription = "${selectedTool.title} dönüştürme işlemini başlat"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedTool == ToolType.DOCUMENT_SCANNER) "Taray�c�y� Ba�lat" else stringResource(R.string.btn_start_processing),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress & Status Feedback Card
                ProgressStatusCard(
                    state = processingState,
                    onReset = { viewModel.resetForm() },
                    onReportError = onNavigateToFeedback?.let { callback ->
                        {
                            val srcFmt = viewModel.file1Uri.value?.lastPathSegment?.substringAfterLast('.', "")?.uppercase() ?: "MP4"
                            val tgtFmt = when (selectedTool) {
                                ToolType.VIDEO_TO_AUDIO -> viewModel.v2aFormat.value.uppercase()
                                ToolType.AUDIO_TO_AUDIO -> viewModel.a2aFormat.value.uppercase()
                                ToolType.VIDEO_TO_VIDEO -> viewModel.v2vFormat.value.uppercase()
                                ToolType.AUDIO_TO_VIDEO -> "MP4"
                                ToolType.IMAGE_TO_IMAGE -> viewModel.i2iFormat.value.uppercase()
                                ToolType.MERGE_VIDEOS -> "MP4"
                                ToolType.MERGE_AUDIOS -> "MP3"
                                else -> "MP3"
                            }
                            callback(selectedTool.title, "Dosya işlenemedi", srcFmt.ifBlank { "MP4" }, tgtFmt)
                        }
                    }
                )
            }
        }
    }
}
