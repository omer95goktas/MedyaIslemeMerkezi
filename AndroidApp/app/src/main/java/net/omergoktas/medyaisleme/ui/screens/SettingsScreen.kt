package net.omergoktas.medyaisleme.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.data.model.ThemeMode
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget
import net.omergoktas.medyaisleme.ui.viewmodel.ThemeViewModel

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by themeViewModel.themeMode.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Theme Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Uygulama Teması ve Görünüm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ThemeOptionItem(
                    title = "Sistem Varsayılanı",
                    subtitle = "Cihazın sistem tema ayarlarına otomatik uyum sağlar",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) }
                )

                ThemeOptionItem(
                    title = "Koyu Tema (Dark Mode)",
                    subtitle = "Gece ve düşük ışıkta göz yormayan koyu renk paleti",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) }
                )

                ThemeOptionItem(
                    title = "Açık Tema (Light Mode)",
                    subtitle = "Aydınlık ve ferah görünüm",
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) }
                )

                ThemeOptionItem(
                    title = "Dinamik Renkler (Material You)",
                    subtitle = "Android 12+ cihaz duvar kağıdı renklerine göre uyarlanır",
                    icon = Icons.Default.ColorLens,
                    isSelected = currentTheme == ThemeMode.DYNAMIC,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.DYNAMIC) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Uygulama Bilgileri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                val versionName = androidx.compose.runtime.remember(context) {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.6"
                    } catch (e: Exception) {
                        "1.0.6"
                    }
                }

                InfoRow(label = "Uygulama Adı", value = "Medya ve Belge İşleme Merkezi")
                InfoRow(label = "Versiyon", value = versionName)
                InfoRow(label = "Geliştirici", value = "Ömer Göktaş (omergoktas.net)")
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "$title temasını seç",
                onClick = onClick
            )
            .accessibleTouchTarget()
            .semantics {
                contentDescription = "$title. $subtitle.${if (isSelected) " Seçili." else ""}"
                role = Role.RadioButton
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.accessibleTouchTarget()
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
