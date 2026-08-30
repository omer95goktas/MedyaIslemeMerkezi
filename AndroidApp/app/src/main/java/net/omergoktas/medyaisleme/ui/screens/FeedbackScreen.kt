package net.omergoktas.medyaisleme.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import net.omergoktas.medyaisleme.data.api.ApiClient
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun FeedbackScreen(
    modifier: Modifier = Modifier,
    preselectedTool: String = "",
    preselectedErrorType: String = "",
    preselectedSourceFormat: String = "",
    preselectedTargetFormat: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val toolOptions = listOf(
        "Videodan Sese", "Ses Formatı Değiştir", "Video Formatı / GIF",
        "Sesten Video Oluştur", "Resim Formatı Değiştir", "Video Birleştir",
        "Ses Birleştir", "Belge Dönüştür", "PDF OCR", "Resimden Yazı Oku",
        "PDF Böl / Birleştir", "Belge Oluşturucu", "Diğer"
    )
    val errorTypeOptions = listOf(
        "Sunucu işlem hatası (500)", "Dosya işlenemedi", "Dosya yüklenirken hata",
        "Dosya boyutu aşıldı", "İndirme hatası", "Uygulama dondu / kapandı", "Bağlantı hatası", "Diğer"
    )
    var selectedTool by remember { mutableStateOf(preselectedTool.ifBlank { toolOptions[0] }) }
    
    val dynamicSourceFormatOptions = remember(selectedTool) {
        val tl = selectedTool.lowercase()
        when {
            tl.contains("video") && tl.contains("ses") -> listOf("MP4", "MKV", "AVI", "MOV", "WEBM", "Diğer")
            tl.contains("ses format") -> listOf("MP3", "WAV", "AAC", "M4A", "FLAC", "OGG", "Diğer")
            tl.contains("video format") || tl.contains("birleştir") -> listOf("MP4", "MKV", "AVI", "MOV", "WEBM", "Diğer")
            tl.contains("sesten video") -> listOf("JPG", "PNG", "MP3", "WAV", "Diğer")
            tl.contains("resim") -> listOf("JPG", "PNG", "WEBP", "BMP", "Diğer")
            tl.contains("ocr") -> listOf("PDF", "JPG", "PNG", "WEBP", "Diğer")
            tl.contains("belge") || tl.contains("pdf") -> listOf("PDF", "DOCX", "EPUB", "TXT", "HTML", "MD", "CSV", "Diğer")
            else -> listOf("MP4", "MP3", "JPG", "PDF", "DOCX", "Diğer")
        }
    }
    val dynamicTargetFormatOptions = remember(selectedTool) {
        val tl = selectedTool.lowercase()
        when {
            tl.contains("video") && tl.contains("ses") -> listOf("MP3", "WAV", "AAC", "M4A", "FLAC", "Diğer")
            tl.contains("ses format") -> listOf("MP3", "WAV", "AAC", "M4A", "FLAC", "OGG", "Diğer")
            tl.contains("video format") || tl.contains("birleştir") -> listOf("MP4", "GIF", "MKV", "WEBM", "AVI", "Diğer")
            tl.contains("sesten video") -> listOf("MP4", "Diğer")
            tl.contains("resim") -> listOf("JPG", "PNG", "WEBP", "PDF", "ICO", "Diğer")
            tl.contains("ocr") -> listOf("TXT", "Diğer")
            tl.contains("belge") || tl.contains("pdf") -> listOf("PDF", "DOCX", "EPUB", "TXT", "HTML", "MD", "CSV", "Diğer")
            else -> listOf("MP4", "MP3", "JPG", "PDF", "DOCX", "Diğer")
        }
    }

    var selectedErrorType by remember { mutableStateOf(preselectedErrorType.ifBlank { errorTypeOptions[0] }) }
    var selectedSourceFormat by remember { mutableStateOf(preselectedSourceFormat.uppercase().replace(".", "").ifBlank { "MP4" }) }
    var selectedTargetFormat by remember { mutableStateOf(preselectedTargetFormat.uppercase().replace(".", "").ifBlank { "MP3" }) }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var toolDropdownExpanded by remember { mutableStateOf(false) }
    var errorTypeDropdownExpanded by remember { mutableStateOf(false) }
    var sourceFormatDropdownExpanded by remember { mutableStateOf(false) }
    var targetFormatDropdownExpanded by remember { mutableStateOf(false) }

    val isFormValid = description.trim().length >= 15 && selectedSourceFormat.isNotBlank() && selectedTargetFormat.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Hata Bildir / Geri Bildirim",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Yaşadığınız sorunun hızlıca çözülebilmesi için hata detaylarını eksiksiz doldurun.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isSent) {
            Card(
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "Geri bildiriminiz başarıyla iletildi. Teşekkürler."
                    liveRegion = LiveRegionMode.Assertive
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Teşekkürler!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Geri bildiriminiz başarıyla iletildi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { isSent = false; description = ""; errorMessage = "" },
                modifier = Modifier.fillMaxWidth().accessibleTouchTarget()
                    .semantics { contentDescription = "Yeni bildirim gönder butonu" }
            ) { Text("Yeni Bildirim Gönder") }
        } else {
            // 1. Araç Seçimi
            Text(
                "Kullanılan Araç", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = toolDropdownExpanded,
                onExpandedChange = { toolDropdownExpanded = !toolDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTool, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toolDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().accessibleTouchTarget()
                        .semantics { contentDescription = "Araç seçimi: $selectedTool" },
                    label = { Text("Araç Seçin") }, singleLine = true
                )
                ExposedDropdownMenu(expanded = toolDropdownExpanded, onDismissRequest = { toolDropdownExpanded = false }) {
                    toolOptions.forEach { tool ->
                        DropdownMenuItem(
                            text = { Text(tool) },
                            onClick = { selectedTool = tool; toolDropdownExpanded = false },
                            modifier = Modifier.accessibleTouchTarget().semantics { contentDescription = tool }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hata Türü
            Text(
                "Karşılaşılan Hata Türü", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = errorTypeDropdownExpanded,
                onExpandedChange = { errorTypeDropdownExpanded = !errorTypeDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedErrorType, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = errorTypeDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().accessibleTouchTarget()
                        .semantics { contentDescription = "Hata türü: $selectedErrorType" },
                    label = { Text("Hata Türü") }, singleLine = true
                )
                ExposedDropdownMenu(expanded = errorTypeDropdownExpanded, onDismissRequest = { errorTypeDropdownExpanded = false }) {
                    errorTypeOptions.forEach { errorType ->
                        DropdownMenuItem(
                            text = { Text(errorType) },
                            onClick = { selectedErrorType = errorType; errorTypeDropdownExpanded = false },
                            modifier = Modifier.accessibleTouchTarget().semantics { contentDescription = errorType }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Kaynak Format
            Text(
                "Yüklenen Dosya Formatı (Kaynak)", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = sourceFormatDropdownExpanded,
                onExpandedChange = { sourceFormatDropdownExpanded = !sourceFormatDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSourceFormat, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceFormatDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().accessibleTouchTarget()
                        .semantics { contentDescription = "Yüklenen dosya formatı: $selectedSourceFormat" },
                    label = { Text("Kaynak Format") }, singleLine = true
                )
                ExposedDropdownMenu(expanded = sourceFormatDropdownExpanded, onDismissRequest = { sourceFormatDropdownExpanded = false }) {
                    dynamicSourceFormatOptions.forEach { fmt ->
                        DropdownMenuItem(
                            text = { Text(fmt) },
                            onClick = { selectedSourceFormat = fmt; sourceFormatDropdownExpanded = false },
                            modifier = Modifier.accessibleTouchTarget().semantics { contentDescription = fmt }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Hedef Format
            Text(
                "Dönüştürülmek İstenen Format (Hedef)", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = targetFormatDropdownExpanded,
                onExpandedChange = { targetFormatDropdownExpanded = !targetFormatDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTargetFormat, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetFormatDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().accessibleTouchTarget()
                        .semantics { contentDescription = "Dönüştürülmek istenen format: $selectedTargetFormat" },
                    label = { Text("Hedef Format") }, singleLine = true
                )
                ExposedDropdownMenu(expanded = targetFormatDropdownExpanded, onDismissRequest = { targetFormatDropdownExpanded = false }) {
                    dynamicTargetFormatOptions.forEach { fmt ->
                        DropdownMenuItem(
                            text = { Text(fmt) },
                            onClick = { selectedTargetFormat = fmt; targetFormatDropdownExpanded = false },
                            modifier = Modifier.accessibleTouchTarget().semantics { contentDescription = fmt }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Açıklama Alanı (Min 15 Karakter)
            val charLen = description.trim().length
            val charHelpText = if (charLen < 15) {
                "Karakter: $charLen / 15 (En az ${15 - charLen} karakter daha yazmalısınız)"
            } else {
                "Karakter: $charLen / 500 (Uygun)"
            }

            Text(
                "Hata Açıklaması (En az 15 karakter)", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                    .semantics { contentDescription = "Hata açıklama alanı. $charHelpText" },
                label = { Text("Yaşadığınız sorunu kısaca anlatın...") },
                maxLines = 6,
                supportingText = {
                    Text(
                        text = charHelpText,
                        color = if (charLen >= 15) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            )

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.semantics {
                        contentDescription = "Hata: $errorMessage"
                        liveRegion = LiveRegionMode.Assertive
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            val toText = { s: String -> s.toRequestBody("text/plain".toMediaTypeOrNull()) }
                            val androidVer = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
                            
                            val appVerName = try {
                                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                                "v${pInfo.versionName}"
                            } catch (_: Exception) {
                                "v1.0.9"
                            }

                            val response = ApiClient.service.submitFeedback(
                                toolName = toText(selectedTool),
                                errorType = toText(selectedErrorType),
                                sourceFormat = toText(selectedSourceFormat),
                                targetFormat = toText(selectedTargetFormat),
                                description = toText(description),
                                websiteUrl = null,
                                androidVersion = toText(androidVer),
                                appVersion = toText(appVerName)
                            )

                            if (response.isSuccessful) {
                                isSent = true
                            } else {
                                var errDetail = "Gönderim başarısız. Lütfen tekrar deneyin."
                                try {
                                    val errStr = response.errorBody()?.string()
                                    if (!errStr.isNullOrBlank()) {
                                        val json = Gson().fromJson(errStr, JsonObject::class.java)
                                        if (json.has("detail")) {
                                            errDetail = json.get("detail").asString
                                        }
                                    }
                                } catch (_: Exception) {}
                                errorMessage = errDetail
                            }
                        } catch (e: Exception) {
                            errorMessage = "Bağlantı hatası. İnternet bağlantınızı kontrol edin."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && isFormValid,
                modifier = Modifier.fillMaxWidth().accessibleTouchTarget()
                    .semantics {
                        contentDescription = if (isLoading) "Gönderiliyor, lütfen bekleyin" else "Bildirimi gönder butonu"
                    }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gönderiliyor...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bildirimi Gönder", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
