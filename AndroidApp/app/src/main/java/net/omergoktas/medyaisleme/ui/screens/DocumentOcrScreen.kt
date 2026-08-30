package net.omergoktas.medyaisleme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.R
import net.omergoktas.medyaisleme.data.model.ProcessingState
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
fun DocumentOcrScreen(
    viewModel: ProcessingViewModel,
    onNavigateToFeedback: ((tool: String, errorType: String, sourceFormat: String, targetFormat: String) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToDocCreator: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedTool by viewModel.selectedTool.collectAsState()
    val processingState by viewModel.processingState.collectAsState()

    val file1Uri by viewModel.file1Uri.collectAsState()
    val file2Uri by viewModel.file2Uri.collectAsState()
    val docFormat by viewModel.docFormat.collectAsState()

    val docTools = ToolType.entries.filter { it.category == ToolCategory.DOCUMENT }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (selectedTool.category != ToolCategory.DOCUMENT) {
            docTools.firstOrNull()?.let { viewModel.selectTool(it) }
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
        val toolOptions = docTools.map { tool ->
            DropdownOption(
                value = tool.id,
                title = tool.title,
                subtitle = tool.description
            )
        }

        AccessibleDropdown(
            label = "📄 Kullanmak İstediğiniz Belge/OCR Aracını Seçin:",
            options = toolOptions,
            selectedValue = selectedTool.id,
            onValueSelected = { toolId ->
                docTools.find { it.id == toolId }?.let { viewModel.selectTool(it) }
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

                when (selectedTool) {
                    ToolType.DOCUMENT_CONVERT -> {
                        FilePickerField(
                            label = "Belge Dosyası Seçin:",
                            mimeType = "*/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            hint = "Desteklenen türler: PDF, DOCX, DOC, ODT, RTF, TXT, HTML, EPUB, XLSX, CSV, MD",
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccessibleDropdown(
                            label = "Hedef Format:",
                            options = listOf(
                                DropdownOption("pdf", "PDF Belgesi (.pdf)"),
                                DropdownOption("docx", "Microsoft Word Belgesi (.docx)"),
                                DropdownOption("md", "Markdown Belgesi (.md)"),
                                DropdownOption("epub", "E-Kitap Formatı (.epub)"),
                                DropdownOption("txt", "Düz Metin (.txt)"),
                                DropdownOption("html", "Web Sayfası (.html)"),
                                DropdownOption("odt", "OpenDocument Metni (.odt)"),
                                DropdownOption("rtf", "Zengin Metin Biçimi (.rtf)"),
                                DropdownOption("csv", "CSV Tablo Belgesi (.csv)")
                            ),
                            selectedValue = docFormat,
                            onValueSelected = { viewModel.docFormat.value = it }
                        )
                    }

                    ToolType.PDF_OCR -> {
                        FilePickerField(
                            label = "Taranmış PDF Dosyası:",
                            mimeType = "application/pdf",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            hint = "Taranmış ve resim formatındaki PDF'leri OCR ile kopyalanabilir ve aranabilir PDF yapar.",
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )
                    }

                    ToolType.IMAGE_TO_TEXT -> {
                        FilePickerField(
                            label = "Metin İçeren Resim Seçin:",
                            mimeType = "image/*",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            hint = "Görsel üzerindeki tüm Türkçe/İngilizce metinleri tespit eder ve TXT dosyası olarak sunar.",
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )
                    }

                    ToolType.SPLIT_PDF -> {
                        FilePickerField(
                            label = "Bölünecek PDF Belgesi:",
                            mimeType = "application/pdf",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            hint = "Çok sayfalı PDF belgelerini her sayfası ayrı bir PDF olacak şekilde ZIP içinde paketler.",
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )
                    }

                    ToolType.MERGE_PDFS -> {
                        FilePickerField(
                            label = "1. PDF Belgesi:",
                            mimeType = "application/pdf",
                            selectedUri = file1Uri,
                            onUriSelected = { viewModel.file1Uri.value = it },
                            maxSizeLabel = "1. PDF"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FilePickerField(
                            label = "2. PDF Belgesi:",
                            mimeType = "application/pdf",
                            selectedUri = file2Uri,
                            onUriSelected = { viewModel.file2Uri.value = it },
                            maxSizeLabel = "Maksimum dosya boyutu: 1 GB"
                        )
                    }

                    ToolType.DOCUMENT_CREATE -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onNavigateToDocCreator?.invoke() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .accessibleTouchTarget()
                                .semantics {
                                    contentDescription = "Belge oluşturucu ekranını aç"
                                    role = Role.Button
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Belge Oluşturucuyu Aç",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    else -> {}
                }

                if (selectedTool != ToolType.DOCUMENT_CREATE) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = { viewModel.startProcessing() },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .accessibleTouchTarget()
                            .semantics {
                                contentDescription = "${selectedTool.title} işlemini başlat ve indir"
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

                    // Progress Status Card
                    ProgressStatusCard(
                        state = processingState,
                        onReset = { viewModel.resetForm() },
                        onReportError = onNavigateToFeedback?.let { callback ->
                            {
                                val srcFmt = viewModel.file1Uri.value?.lastPathSegment?.substringAfterLast('.', "")?.uppercase() ?: "PDF"
                                val tgtFmt = when (selectedTool) {
                                    ToolType.DOCUMENT_CONVERT -> viewModel.docFormat.value.uppercase()
                                    ToolType.PDF_OCR -> "OCR_PDF"
                                    ToolType.IMAGE_TO_TEXT -> "OCR_TXT"
                                    ToolType.SPLIT_PDF -> "ZIP_PDF"
                                    ToolType.MERGE_PDFS -> "MERGE_PDF"
                                    else -> "PDF"
                                }
                                callback(selectedTool.title, "Dosya işlenemedi", srcFmt.ifBlank { "PDF" }, tgtFmt)
                            }
                        }
                    )
                }
            }
        }
    }
}
