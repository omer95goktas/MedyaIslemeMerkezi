package net.omergoktas.medyaisleme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.R
import net.omergoktas.medyaisleme.data.download.FileDownloadHelper
import net.omergoktas.medyaisleme.data.model.ProcessingState
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import net.omergoktas.medyaisleme.ui.accessibility.politeLiveRegion
import net.omergoktas.medyaisleme.ui.theme.ErrorRose
import net.omergoktas.medyaisleme.ui.theme.PrimarySky
import net.omergoktas.medyaisleme.ui.theme.SuccessEmerald

@Composable
fun ProgressStatusCard(
    state: ProcessingState,
    onReset: () -> Unit,
    onReportError: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = state !is ProcessingState.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .politeLiveRegion()
        ) {
            when (state) {
                is ProcessingState.Uploading -> {
                    val a11yMsg = "Yükleniyor: yüzde ${state.progressPercent}"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimarySky.copy(alpha = 0.12f))
                            .border(1.dp, PrimarySky, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .semantics { contentDescription = a11yMsg },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Yükleniyor: %${state.progressPercent}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimarySky,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                is ProcessingState.ServerProcessing -> {
                    val serverMsg = stringResource(R.string.status_server_processing)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimarySky.copy(alpha = 0.12f))
                            .border(1.dp, PrimarySky, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .semantics { contentDescription = serverMsg },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PrimarySky,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = serverMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ProcessingState.Downloading -> {
                    val dlMsg = stringResource(R.string.status_downloading)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimarySky.copy(alpha = 0.12f))
                            .border(1.dp, PrimarySky, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .semantics { contentDescription = dlMsg },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PrimarySky,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = dlMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ProcessingState.Success -> {
                    val successMsg = "Tebrikler! İşlem başarıyla tamamlandı. ${state.filename} dosyası İndirilenler klasörüne kaydedildi."
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessEmerald.copy(alpha = 0.15f))
                            .border(1.dp, SuccessEmerald, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .semantics { contentDescription = successMsg },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessEmerald,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tebrikler! İşlem Başarıyla Tamamlandı.",
                            style = MaterialTheme.typography.titleMedium,
                            color = SuccessEmerald,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.filename,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (state.fileUri != null) {
                                Button(
                                    onClick = {
                                        FileDownloadHelper.openFile(context, state.fileUri)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .accessibleTouchTarget(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dosyayı Aç", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = onReset,
                                modifier = Modifier
                                    .weight(1f)
                                    .accessibleTouchTarget(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yeni İşlem", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is ProcessingState.Error -> {
                    val errorMsg = state.message
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRose.copy(alpha = 0.15f))
                            .border(1.dp, ErrorRose, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .semantics { contentDescription = "Hata: $errorMsg" },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRose,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onReset,
                            modifier = Modifier
                                .fillMaxWidth()
                                .accessibleTouchTarget(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_retry),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (onReportError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onReportError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .accessibleTouchTarget()
                                    .semantics { contentDescription = "Hatayı bildir butonu. Telegram üzerinden hata bildirimi gönder." },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = ErrorRose,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚔 Hatayı Bildir",
                                    color = ErrorRose,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
