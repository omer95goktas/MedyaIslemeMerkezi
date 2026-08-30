package net.omergoktas.medyaisleme.ui.accessibility

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

object AccessibilityHelper {

    fun announce(context: Context, view: View?, message: String) {
        if (message.isBlank()) return
        
        view?.announceForAccessibility(message) ?: run {
            val accessibilityManager =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            if (accessibilityManager?.isEnabled == true) {
                val event = AccessibilityEvent.obtain().apply {
                    eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                    className = javaClass.name
                    packageName = context.packageName
                    text.add(message)
                }
                accessibilityManager.sendAccessibilityEvent(event)
            }
        }
    }
}

/**
 * Ensures minimum touch target size according to Android Accessibility standard (48x48 dp)
 */
fun Modifier.accessibleTouchTarget(): Modifier = this.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)

/**
 * Semantics modifier for live region announcements in Compose
 */
fun Modifier.politeLiveRegion(): Modifier = this.semantics {
    liveRegion = LiveRegionMode.Polite
}

fun Modifier.assertiveLiveRegion(): Modifier = this.semantics {
    liveRegion = LiveRegionMode.Assertive
}
