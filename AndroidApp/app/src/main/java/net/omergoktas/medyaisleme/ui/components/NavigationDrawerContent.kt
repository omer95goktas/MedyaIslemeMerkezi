package net.omergoktas.medyaisleme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.R
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget

enum class NavDestination(val title: String) {
    HOME("Ana Sayfa"),
    AUDIO_TOOLS("Ses İşlemleri"),
    VIDEO_TOOLS("Video İşlemleri"),
    DOCUMENT_TOOLS("Belge / Doküman İşlemleri"),
    DOCUMENT_CREATOR("Belge Oluşturucu"),
    PRIVACY_POLICY("Gizlilik Politikası"),
    SETTINGS("Ayarlar"),
    FEEDBACK("Geri Bildirim")
}

@Composable
fun NavigationDrawerContent(
    currentDestination: NavDestination,
    onNavigateToDestination: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "medya.omergoktas.net",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        // 1. Ses İşlemleri
        NavigationDrawerItem(
            label = { Text("🎵 Ses İşlemleri", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            selected = currentDestination == NavDestination.AUDIO_TOOLS,
            onClick = { onNavigateToDestination(NavDestination.AUDIO_TOOLS) },
            icon = { Icon(Icons.Default.Audiotrack, contentDescription = null) },
            modifier = Modifier.padding(vertical = 4.dp).accessibleTouchTarget()
                .semantics { contentDescription = "Ses işleme araçlarını aç" }
        )

        // 2. Video İşlemleri
        NavigationDrawerItem(
            label = { Text("🎬 Video İşlemleri", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            selected = currentDestination == NavDestination.VIDEO_TOOLS,
            onClick = { onNavigateToDestination(NavDestination.VIDEO_TOOLS) },
            icon = { Icon(Icons.Default.Movie, contentDescription = null) },
            modifier = Modifier.padding(vertical = 4.dp).accessibleTouchTarget()
                .semantics { contentDescription = "Video işleme araçlarını aç" }
        )

        // 3. Belge / Doküman İşlemleri
        NavigationDrawerItem(
            label = { Text("📄 Belge / Doküman İşlemleri", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            selected = currentDestination == NavDestination.DOCUMENT_TOOLS || currentDestination == NavDestination.DOCUMENT_CREATOR,
            onClick = { onNavigateToDestination(NavDestination.DOCUMENT_TOOLS) },
            icon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier.padding(vertical = 4.dp).accessibleTouchTarget()
                .semantics { contentDescription = "Belge ve doküman araçlarını aç" }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        // 4. Ayarlar
        NavigationDrawerItem(
            label = { Text("⚙️ Ayarlar", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            selected = currentDestination == NavDestination.SETTINGS,
            onClick = { onNavigateToDestination(NavDestination.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(vertical = 4.dp).accessibleTouchTarget()
                .semantics { contentDescription = "Ayarlar sayfasını aç" }
        )

        // 5. Gizlilik Politikası
        NavigationDrawerItem(
            label = { Text("🔒 Gizlilik Politikası", fontSize = 15.sp, fontWeight = FontWeight.Medium) },
            selected = currentDestination == NavDestination.PRIVACY_POLICY,
            onClick = { onNavigateToDestination(NavDestination.PRIVACY_POLICY) },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            modifier = Modifier.padding(vertical = 4.dp).accessibleTouchTarget()
                .semantics { contentDescription = "Gizlilik Politikası sayfasını aç" }
        )
    }
}
