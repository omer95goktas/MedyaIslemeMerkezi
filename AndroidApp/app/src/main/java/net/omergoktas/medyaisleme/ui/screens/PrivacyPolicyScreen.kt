package net.omergoktas.medyaisleme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget

@Composable
fun PrivacyPolicyScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    BackHandler {
        onBackToHome()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔒 Gizlilik Politikası ve Güvenlik İlkeleri",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Bu platform, kullanıcıların dijital medya dosyalarını (video, ses, görsel) ve ofis belgelerini (Word, PDF, Markdown, EPUB, TXT) güvenli, hızlı ve gizlilik odaklı bir şekilde dönüştürmesi, işlemesi ve oluşturması amacıyla geliştirilmiştir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                PolicySectionItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "1. Dosyaların Geçiciliği ve Otomatik Silinme",
                    content = "Platformumuza yüklediğiniz hiçbir dosya sunucuda kalıcı olarak saklanmaz, arşivlenmez veya üçüncü şahıslarla paylaşılmaz:\n• Anlık Silinme: Dönüştürme, ayıklama, birleştirme veya OCR işlemi tamamlanıp dosyanız cihazınıza indirildiği anda geçici dosyalar sunucudan anında ve kalıcı olarak silinir.\n• Arka Plan Temizliği: Olası bağlantı kopmaları veya yarım kalan işlemler durumunda geride kalabilecek tüm geçici veriler, periyodik temizlik mekanizmalarıyla tamamen yok edilir."
                )

                PolicySectionItem(
                    icon = Icons.Default.Lock,
                    title = "2. Belge Oluşturucu ve Metin Güvenliği",
                    content = "Sıfırdan Belge Oluşturucu aracında yazdığınız metinler ve içerikler sadece oluşturulmak istenen dosya formatına (Word, PDF, Markdown, EPUB vb.) dönüştürülmek üzere işlenir. Yazdığınız metinler hiçbir veritabanına kaydedilmez."
                )

                PolicySectionItem(
                    icon = Icons.Default.Scanner,
                    title = "3. OCR ve Görüntü İşleme",
                    content = "Taranmış PDF'ler veya resimlerden çıkarılan metinler yalnızca dönüşüm süresince bellekte tutulur ve işlem bittiğinde tamamen imha edilir."
                )

                PolicySectionItem(
                    icon = Icons.Default.Security,
                    title = "4. Kayıt Tutmama (No-Log) Prensibi",
                    content = "Sunucularımızda yüklediğiniz dosyaların içeriğine, belge başlıklarına veya kişisel verilerinize dair herhangi bir içerik günlüğü (content log) tutulmamaktadır."
                )

                PolicySectionItem(
                    icon = Icons.Default.Mail,
                    title = "5. İletişim",
                    content = "Gizlilik ilkeleri veya sistem güvenliği ile ilgili tüm sorularınız için doğrudan mail@omergoktas.net adresine ulaşabilirsiniz."
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .accessibleTouchTarget()
                        .semantics {
                            contentDescription = "Uygulamaya dön butonu"
                            role = Role.Button
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("← Uygulamaya Dön", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PolicySectionItem(
    icon: ImageVector,
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(start = 28.dp)
        )
    }
}
