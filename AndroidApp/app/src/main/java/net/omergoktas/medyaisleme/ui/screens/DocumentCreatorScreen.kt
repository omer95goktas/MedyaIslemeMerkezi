package net.omergoktas.medyaisleme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.R
import net.omergoktas.medyaisleme.data.model.ProcessingState
import net.omergoktas.medyaisleme.data.model.ToolType
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import net.omergoktas.medyaisleme.ui.components.AccessibleDropdown
import net.omergoktas.medyaisleme.ui.components.DropdownOption
import net.omergoktas.medyaisleme.ui.components.ProgressStatusCard
import net.omergoktas.medyaisleme.ui.components.SloganBanner
import net.omergoktas.medyaisleme.ui.viewmodel.ProcessingViewModel

@Composable
fun DocumentCreatorScreen(
    viewModel: ProcessingViewModel,
    modifier: Modifier = Modifier
) {
    val processingState by viewModel.processingState.collectAsState()
    val createDocName by viewModel.createDocName.collectAsState()
    val createDocFormat by viewModel.createDocFormat.collectAsState()

    var textValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "<h1>Yeni Belge Başlığı</h1>\n<p>Buraya belgenizin detaylarını, notlarını veya makalenizi yazabilirsiniz.</p>\n<ul>\n  <li>Madde 1</li>\n  <li>Madde 2</li>\n</ul>"
            )
        )
    }

    val scrollState = rememberScrollState()
    val toolbarScrollState = rememberScrollState()

    val isProcessing = processingState is ProcessingState.Uploading ||
            processingState is ProcessingState.ServerProcessing ||
            processingState is ProcessingState.Downloading

    fun insertTag(openTag: String, closeTag: String) {
        val min = textValue.selection.min
        val max = textValue.selection.max
        val selectedText = if (min != max && min >= 0 && max <= textValue.text.length) {
            textValue.text.substring(min, max)
        } else {
            ""
        }
        val newText = if (selectedText.isNotEmpty()) {
            val before = textValue.text.substring(0, min)
            val after = textValue.text.substring(max)
            "$before$openTag$selectedText$closeTag$after"
        } else {
            val cursor = textValue.selection.start
            val before = textValue.text.substring(0, cursor)
            val after = textValue.text.substring(cursor)
            "$before$openTag$closeTag$after"
        }
        val newCursorPos = min + openTag.length + selectedText.length
        textValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
        viewModel.createDocHtml.value = newText
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Slogan Banner
        SloganBanner()

        Spacer(modifier = Modifier.height(16.dp))

        // Main Card
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
                    text = "✍️ Sıfırdan Zengin Belge Oluşturucu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Metninizi zengin editörde yazın; Word, PDF, Markdown veya EPUB olarak indirin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Belge Adı
                OutlinedTextField(
                    value = createDocName,
                    onValueChange = { viewModel.createDocName.value = it },
                    label = { Text("Belge Adı (İsteğe Bağlı)") },
                    placeholder = { Text("Örn: ders_notlari veya makalem") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .accessibleTouchTarget()
                        .semantics {
                            contentDescription = "Belge Adı giriş alanı. İsteğe bağlıdır."
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kaydedilecek Format
                AccessibleDropdown(
                    label = "Kaydedilecek Dosya Türü:",
                    options = listOf(
                        DropdownOption("docx", "Microsoft Word Belgesi (.docx)"),
                        DropdownOption("pdf", "PDF Belgesi (.pdf)"),
                        DropdownOption("md", "Markdown Belgesi (.md)"),
                        DropdownOption("epub", "E-Kitap Formatı (.epub)"),
                        DropdownOption("txt", "Düz Metin (.txt)"),
                        DropdownOption("html", "Web Sayfası / HTML (.html)")
                    ),
                    selectedValue = createDocFormat,
                    onValueSelected = { viewModel.createDocFormat.value = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Belge İçeriği & Zengin Araç Çubuğu:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rich Editor Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .horizontalScroll(toolbarScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bold
                    ToolbarButton(label = "B", desc = "Kalın metin ekle") {
                        insertTag("<b>", "</b>")
                    }
                    // Italic
                    ToolbarButton(label = "I", desc = "İtalik metin ekle") {
                        insertTag("<i>", "</i>")
                    }
                    // Underline
                    ToolbarButton(label = "U", desc = "Altı çizili metin ekle") {
                        insertTag("<u>", "</u>")
                    }
                    // H1
                    ToolbarButton(label = "H1", desc = "Büyük başlık ekle") {
                        insertTag("<h1>", "</h1>")
                    }
                    // H2
                    ToolbarButton(label = "H2", desc = "Alt başlık ekle") {
                        insertTag("<h2>", "</h2>")
                    }
                    // Bullet List
                    ToolbarButton(label = "• Liste", desc = "Madde imli liste ekle") {
                        insertTag("<ul>\n  <li>", "</li>\n</ul>")
                    }
                    // Numbered List
                    ToolbarButton(label = "1. Liste", desc = "Numaralandırılmış liste ekle") {
                        insertTag("<ol>\n  <li>", "</li>\n</ol>")
                    }
                    // Paragraph
                    ToolbarButton(label = "P", desc = "Paragraf ekle") {
                        insertTag("<p>", "</p>")
                    }
                }

                // Editor TextArea
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        viewModel.createDocHtml.value = it.text
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .semantics {
                            contentDescription = "Zengin metin editörü içeriği alanı"
                        },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.createDocHtml.value = textValue.text
                        viewModel.selectTool(ToolType.DOCUMENT_CREATE)
                        viewModel.startProcessing()
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .accessibleTouchTarget()
                        .semantics {
                            contentDescription = "Belgeyi oluştur ve indir"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Belgeyi Oluştur ve İndir",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress & Status Card
                ProgressStatusCard(
                    state = processingState,
                    onReset = { viewModel.resetForm() }
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    desc: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .accessibleTouchTarget()
            .semantics {
                contentDescription = desc
                role = Role.Button
            },
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
