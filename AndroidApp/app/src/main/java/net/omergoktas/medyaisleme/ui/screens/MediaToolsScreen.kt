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
            label = "ğŸ¬ Kullanmak Ä°stediÄŸiniz Medya AracÄ±nÄ± SeÃ§in:",
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
                            label = "Video DosyasÄ± SeÃ§in:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Ã‡Ä±kÄ±ÅŸ Ses FormatÄ±:",
                            options = listOf(
                                DropdownOption("mp3", "MP3", "En popÃ¼ler ve evrensel ses formatÄ± (320 kbps)"),
                                DropdownOption("wav", "WAV", "KayÄ±psÄ±z stÃ¼dyo ve ham ses kalitesi"),
                                DropdownOption("aac", "AAC", "YÃ¼ksek verimli geliÅŸmiÅŸ ses kodlama"),
                                DropdownOption("m4a", "M4A", "Apple ve modern cihazlar iÃ§in optimize"),
                                DropdownOption("m4r", "M4R (iPhone Zil Sesi)", "Apple iPhone Ã¶zel zil sesi formatÄ±"),
                                DropdownOption("flac", "FLAC", "KayÄ±psÄ±z sÄ±kÄ±ÅŸtÄ±rÄ±lmÄ±ÅŸ yÃ¼ksek kalite"),
                                DropdownOption("alac", "ALAC", "Apple Lossless kayÄ±psÄ±z ses formatÄ±"),
                                DropdownOption("aiff", "AIFF", "Apple stÃ¼dyo ve kayÄ±psÄ±z ham ses"),
                                DropdownOption("opus", "OPUS", "YÃ¼ksek kaliteli ve dÃ¼ÅŸÃ¼k gecikmeli"),
                                DropdownOption("ogg", "OGG", "AÃ§Ä±k kaynak Vorbis ses formatÄ±"),
                                DropdownOption("ac3", "AC3 (Dolby Digital)", "Ã‡ok kanallÄ± sinema ses formatÄ±"),
                                DropdownOption("wma", "WMA", "Windows Media Audio formatÄ±"),
                                DropdownOption("mp2", "MP2", "MPEG Audio Layer II yayÄ±n formatÄ±")
                            ),
                            selectedValue = v2aFormat,
                            onValueSelected = { viewModel.v2aFormat.value = it }
                        )
                    }

                    ToolType.AUDIO_TO_AUDIO -> {
                        FilePickerField(
                            label = "Ses DosyasÄ± SeÃ§in:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "DÃ¶nÃ¼ÅŸtÃ¼rÃ¼lecek Format:",
                            options = listOf(
                                DropdownOption("mp3", "MP3", "En popÃ¼ler ve evrensel ses formatÄ± (320 kbps)"),
                                DropdownOption("wav", "WAV", "KayÄ±psÄ±z stÃ¼dyo ve ham ses kalitesi"),
                                DropdownOption("aac", "AAC", "YÃ¼ksek verimli geliÅŸmiÅŸ ses kodlama"),
                                DropdownOption("m4a", "M4A", "Apple ve modern cihazlar iÃ§in optimize"),
                                DropdownOption("m4r", "M4R (iPhone Zil Sesi)", "Apple iPhone Ã¶zel zil sesi formatÄ±"),
                                DropdownOption("flac", "FLAC", "KayÄ±psÄ±z sÄ±kÄ±ÅŸtÄ±rÄ±lmÄ±ÅŸ yÃ¼ksek kalite"),
                                DropdownOption("alac", "ALAC", "Apple Lossless kayÄ±psÄ±z ses formatÄ±"),
                                DropdownOption("aiff", "AIFF", "Apple stÃ¼dyo ve kayÄ±psÄ±z ham ses"),
                                DropdownOption("opus", "OPUS", "YÃ¼ksek kaliteli ve dÃ¼ÅŸÃ¼k gecikmeli"),
                                DropdownOption("ogg", "OGG", "AÃ§Ä±k kaynak Vorbis ses formatÄ±"),
                                DropdownOption("ac3", "AC3 (Dolby Digital)", "Ã‡ok kanallÄ± sinema ses formatÄ±"),
                                DropdownOption("wma", "WMA", "Windows Media Audio formatÄ±"),
                                DropdownOption("mp2", "MP2", "MPEG Audio Layer II yayÄ±n formatÄ±")
                            ),
                            selectedValue = a2aFormat,
                            onValueSelected = { viewModel.a2aFormat.value = it }
                        )
                    }

                    ToolType.VIDEO_TO_VIDEO -> {
                        FilePickerField(
                            label = "Video DosyasÄ± SeÃ§in:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "DÃ¶nÃ¼ÅŸtÃ¼rÃ¼lecek Video FormatÄ±:",
                            options = listOf(
                                DropdownOption("mp4", "MP4 (Evrensel Format)"),
                                DropdownOption("gif", "GIF Animasyon (GeliÅŸmiÅŸ Ã‡Ã¶zÃ¼nÃ¼rlÃ¼k ve FPS)"),
                                DropdownOption("mkv", "MKV FormatÄ±"),
                                DropdownOption("webm", "WEBM FormatÄ±"),
                                DropdownOption("avi", "AVI FormatÄ±")
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
                                    text = "âš™ï¸ Profesyonel GIF AyarlarÄ±",
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
                                        label = { Text("BaÅŸlangÄ±Ã§ Saniyesi") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = gifDuration,
                                        onValueChange = { viewModel.gifDuration.value = it },
                                        label = { Text("SÃ¼re (Sn - Max 25)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                AccessibleDropdown(
                                    label = "GIF Ã‡Ã¶zÃ¼nÃ¼rlÃ¼ÄŸÃ¼ / Boyut:",
                                    options = listOf(
                                        DropdownOption("480", "480p (Ã–nerilen - Dengeli)"),
                                        DropdownOption("640", "640p (GeniÅŸ Ekran - YÃ¼ksek Kalite)"),
                                        DropdownOption("720", "720p (Ultra HD - Maksimum Netlik)"),
                                        DropdownOption("320", "320p (DÃ¼ÅŸÃ¼k Boyut - HÄ±zlÄ±)"),
                                        DropdownOption("240", "240p (Mini Boyut - Forum / Avatar)")
                                    ),
                                    selectedValue = gifWidth,
                                    onValueSelected = { viewModel.gifWidth.value = it }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                AccessibleDropdown(
                                    label = "Kare HÄ±zÄ± (FPS / AkÄ±cÄ±lÄ±k):",
                                    options = listOf(
                                        DropdownOption("15", "15 FPS (Ã–nerilen - AkÄ±cÄ± & Optimize)"),
                                        DropdownOption("24", "24 FPS (Sinematik - Ã‡ok AkÄ±cÄ±)"),
                                        DropdownOption("30", "30 FPS (Ultra AkÄ±cÄ±)"),
                                        DropdownOption("10", "10 FPS (Ekonomik - DÃ¼ÅŸÃ¼k Boyut)")
                                    ),
                                    selectedValue = gifFps,
                                    onValueSelected = { viewModel.gifFps.value = it }
                                )
                            }
                        }
                    }

                    ToolType.AUDIO_TO_VIDEO -> {
                        FilePickerField(
                            label = "Ses DosyasÄ± SeÃ§in:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "Kapak Resmi SeÃ§in:",
                            mimeType = "image/*",
                            selectedUri = imageUri,
                            onUriSelected = { viewModel.imageUri.value = it },
                            maxSizeLabel = "JPG / PNG / WEBP GÃ¶rsel"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "3. Video Ã‡Ã¶zÃ¼nÃ¼rlÃ¼ÄŸÃ¼ & En-Boy OranÄ±:",
                            options = listOf(
                                DropdownOption("1080p_horizontal", "â­ 1080p Full HD Yatay (1920x1080 - VarsayÄ±lan / YouTube / PC)"),
                                DropdownOption("720p_horizontal", "ğŸ“º 720p HD Yatay (1280x720 - Standart Web / HÄ±zlÄ± Ä°ÅŸleme)"),
                                DropdownOption("480p_horizontal", "ğŸ“± 480p SD Yatay (854x480 - KÃ¼Ã§Ã¼k Dosya Boyutu)"),
                                DropdownOption("1440p_horizontal", "ğŸ’ 2K QHD Yatay (2560x1440 - YÃ¼ksek Kalite)"),
                                DropdownOption("4k_horizontal", "ğŸ‘‘ 4K Ultra HD Yatay (3840x2160 - Maksimum Kalite)"),
                                DropdownOption("1080p_vertical", "ğŸ“² 1080p Full HD Dikey (1080x1920 - Reels / Shorts / TikTok / Hikaye)"),
                                DropdownOption("720p_vertical", "ğŸ“± 720p HD Dikey (720x1280 - HÄ±zlÄ± Dikey)"),
                                DropdownOption("480p_vertical", "ğŸ“¦ 480p SD Dikey (480x854 - DÃ¼ÅŸÃ¼k Boyut Dikey)"),
                                DropdownOption("1440p_vertical", "âœ¨ 2K QHD Dikey (1440x2560 - Net Dikey Video)"),
                                DropdownOption("4k_vertical", "ğŸŒŸ 4K Ultra HD Dikey (2160x3840 - Ultra Dikey)")
                            ),
                            selectedValue = a2vResolution,
                            onValueSelected = { viewModel.a2vResolution.value = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "â„¹ï¸ HiÃ§bir ayar deÄŸiÅŸtirilmezse video otomatik olarak 1080p Full HD (1920x1080) kalitesinde Ã¼retilir. YÃ¼klediÄŸiniz gÃ¶rsel seÃ§tiÄŸiniz formata tam ekran olarak uyarlanacaktÄ±r.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    ToolType.IMAGE_TO_IMAGE -> {
                        FilePickerField(
                            label = "Resim DosyasÄ± SeÃ§in:",
                            mimeType = "image/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "DÃ¶nÃ¼ÅŸtÃ¼rÃ¼lecek Resim FormatÄ±:",
                            options = listOf(
                                DropdownOption("jpg", "JPG / JPEG"),
                                DropdownOption("png", "PNG (Åeffaf / KayÄ±psÄ±z)"),
                                DropdownOption("webp", "WEBP (Modern Web StandardÄ±)"),
                                DropdownOption("pdf", "PDF Belgesi"),
                                DropdownOption("ico", "ICO (Favicon)")
                            ),
                            selectedValue = i2iFormat,
                            onValueSelected = { viewModel.i2iFormat.value = it }
                        )
                    }

                    ToolType.MERGE_VIDEOS -> {
                        FilePickerField(
                            label = "1. Video DosyasÄ±:",
                            mimeType = "video/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "1. Video"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "2. Video DosyasÄ±:",
                            mimeType = "video/*",
                            selectedUri = file2Uri,
                            onUriSelected = { viewModel.file2Uri.value = it },
                            maxSizeLabel = "Maksimum toplam boyut: 2 GB"
                        )
                    }

                    ToolType.MERGE_AUDIOS -> {
                        FilePickerField(
                            label = "1. Ses DosyasÄ±:",
                            mimeType = "audio/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "1. Ses"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "2. Ses DosyasÄ±:",
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
                        
                            viewModel.startProcessing()
                        
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .accessibleTouchTarget()
                        .semantics {
                            contentDescription = "${selectedTool.title} dÃ¶nÃ¼ÅŸtÃ¼rme iÅŸlemini baÅŸlat"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_start_processing),
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
                            callback(selectedTool.title, "Dosya iÅŸlenemedi", srcFmt.ifBlank { "MP4" }, tgtFmt)
                        }
                    }
                )
            }
        }
    }
}

