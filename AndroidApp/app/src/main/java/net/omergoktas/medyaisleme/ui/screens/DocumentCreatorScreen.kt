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
                text = "Yeni Belge Başlığı\n\nBuraya belgenizin detaylarını, notlarını veya makalenizi yazabilirsiniz."
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

                                // Formatting Dropdown
                var selectedFormatTag by remember { mutableStateOf("") }
                AccessibleDropdown(
                    label = "Hızlı Biçimlendirme (HTML Etiketi Ekle):",
                    options = listOf(
                                                DropdownOption("", "Seçiniz..."),
                        DropdownOption("b", "Kalın Metin (Bold)"),
                        DropdownOption("i", "İtalik Metin (Italic)"),
                        DropdownOption("u", "Altı Çizili (Underline)"),
                        DropdownOption("strike", "Üstü Çizili (Strikethrough)"),
                        DropdownOption("h1", "Ana Başlık (H1)"),
                        DropdownOption("h2", "Alt Başlık (H2)"),
                        DropdownOption("h3", "Küçük Başlık (H3)"),
                        DropdownOption("h4", "Daha Küçük Başlık (H4)"),
                        DropdownOption("p", "Paragraf Ekle"),
                        DropdownOption("br", "Satır Atla (BR)"),
                        DropdownOption("hr", "Yatay Çizgi (HR)"),
                        DropdownOption("ul", "Madde İmli Liste (UL)"),
                        DropdownOption("ol", "Numaralı Liste (OL)"),
                        DropdownOption("blockquote", "Alıntı Kutusu (Blockquote)"),
                        DropdownOption("code", "Satır İçi Kod (Code)"),
                        DropdownOption("pre", "Kod Bloğu (Pre)"),
                        DropdownOption("a", "Bağlantı/Link (A)"),
                        DropdownOption("img", "Resim Ekle (IMG)"),
                        DropdownOption("table", "Basit Tablo (Table)")
                    ),
                    selectedValue = selectedFormatTag,
                    onValueSelected = { tag -> 
                        selectedFormatTag = tag
                        when (tag) {
                                                        "b" -> insertTag("<b>", "</b>")
                            "i" -> insertTag("<i>", "</i>")
                            "u" -> insertTag("<u>", "</u>")
                            "strike" -> insertTag("<del>", "</del>")
                            "h1" -> insertTag("<h1>", "</h1>")
                            "h2" -> insertTag("<h2>", "</h2>")
                            "h3" -> insertTag("<h3>", "</h3>")
                            "h4" -> insertTag("<h4>", "</h4>")
                            "p" -> insertTag("<p>", "</p>")
                            "br" -> insertTag("<br/>\n", "")
                            "hr" -> insertTag("<hr/>\n", "")
                            "ul" -> insertTag("<ul>\n  <li>", "</li>\n</ul>")
                            "ol" -> insertTag("<ol>\n  <li>", "</li>\n</ol>")
                            "blockquote" -> insertTag("<blockquote>\n  ", "\n</blockquote>")
                            "code" -> insertTag("<code>", "</code>")
                            "pre" -> insertTag("<pre>\n  ", "\n</pre>")
                            "a" -> insertTag("<a href=\"URL_BURAYA\">", "</a>")
                            "img" -> insertTag("<img src=\"RESIM_LINKI_BURAYA\" alt=\"Açıklama\"/>\n", "")
                            "table" -> insertTag("<table border=\"1\">\n  <tr>\n    <th>Başlık 1</th>\n    <th>Başlık 2</th>\n  </tr>\n  <tr>\n    <td>Veri 1</td>\n    <td>Veri 2</td>\n  </tr>\n</table>\n", "")
                        }
                        selectedFormatTag = "" // reset after insert
                    }
                )

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
                            contentDescription = "Belge oluştur"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Belge Oluştur",
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

