package net.mamby.androidkit.compose.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
public fun EditorSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
public fun EditorFieldPair(
    modifier: Modifier = Modifier,
    first: @Composable RowScope.() -> Unit,
    second: @Composable RowScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val canPlaceSideBySide = maxWidth >= dimensions.cardMinWidth * 2 + dimensions.spaceMedium
        if (canPlaceSideBySide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            ) {
                first()
                second()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            ) {
                Row(Modifier.fillMaxWidth(), content = first)
                Row(Modifier.fillMaxWidth(), content = second)
            }
        }
    }
}

@Composable
public fun SwitchField(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = dimensions.spaceSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Composable
public fun ReadOnlyPickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
): Unit {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        readOnly = true,
        label = { Text(label) },
        trailingIcon = trailingIcon,
    )
}

@Composable
public fun StringListEditor(
    values: List<String>,
    label: String,
    onValuesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    var pendingValue by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
    ) {
        values.forEach { value ->
            InputChip(
                selected = false,
                onClick = {},
                label = { Text(value) },
                trailingIcon = {
                    IconButton(onClick = { onValuesChange(values - value) }) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                },
            )
        }
        OutlinedTextField(
            value = pendingValue,
            onValueChange = { pendingValue = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            trailingIcon = {
                IconButton(
                    enabled = pendingValue.isNotBlank(),
                    onClick = {
                        val candidate = pendingValue.trim()
                        if (candidate.isNotEmpty() && candidate !in values) {
                            onValuesChange(values + candidate)
                            pendingValue = ""
                        }
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.add)
                }
            },
        )
    }
}

@Composable
public fun FormDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = AndroidKitThemeTokens.strings.confirm,
    dismissLabel: String = AndroidKitThemeTokens.strings.cancel,
    content: @Composable () -> Unit,
): Unit {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = content,
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}
