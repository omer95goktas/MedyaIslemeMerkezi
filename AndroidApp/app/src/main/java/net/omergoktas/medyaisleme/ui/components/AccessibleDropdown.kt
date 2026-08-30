package net.omergoktas.medyaisleme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.omergoktas.medyaisleme.ui.accessibility.accessibleTouchTarget

data class DropdownOption(
    val value: String,
    val title: String,
    val subtitle: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleDropdown(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectedOption = options.firstOrNull { it.value == selectedValue } ?: options.firstOrNull()
    val displayText = currentSelectedOption?.title ?: ""

    val a11yDescription = "$label: Seçili değer $displayText. Seçenekleri açmak için dokunun."

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .accessibleTouchTarget()
                    .semantics {
                        contentDescription = a11yDescription
                        role = Role.DropdownList
                    }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selectedValue
                    DropdownMenuItem(
                        text = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (option.subtitle != null) {
                                    Text(
                                        text = option.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            onValueSelected(option.value)
                            expanded = false
                        },
                        modifier = Modifier
                            .accessibleTouchTarget()
                            .semantics {
                                contentDescription = "${option.title}${if (option.subtitle != null) ", ${option.subtitle}" else ""}${if (isSelected) ", Seçildi" else ""}"
                                role = Role.RadioButton
                            }
                    )
                }
            }
        }

        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
