package net.omergoktas.medyaisleme.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omergoktas.medyaisleme.R
import net.omergoktas.medyaisleme.ui.theme.BorderDark
import net.omergoktas.medyaisleme.ui.theme.PanelBgDark
import net.omergoktas.medyaisleme.ui.theme.PrimarySky
import net.omergoktas.medyaisleme.ui.theme.SirenBlue
import net.omergoktas.medyaisleme.ui.theme.SirenRed

@Composable
fun SloganBanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "siren_transition")

    // Flashing red siren dot
    val redAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "red_siren_alpha"
    )

    // Flashing blue siren dot (offset)
    val blueAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blue_siren_alpha"
    )

    val bannerDescription = stringResource(R.string.a11y_slogan_banner)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(50.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50.dp)
            )
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics {
                contentDescription = bannerDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Red Siren Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(if (redAlpha > 0.6f) 1.2f else 0.9f)
                    .clip(CircleShape)
                    .background(SirenRed.copy(alpha = redAlpha))
            )

            // Slogan Text
            Text(
                text = stringResource(R.string.slogan_text),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )

            // Blue Siren Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(if (blueAlpha > 0.6f) 1.2f else 0.9f)
                    .clip(CircleShape)
                    .background(SirenBlue.copy(alpha = blueAlpha))
            )
        }
    }
}
