package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
public fun FloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = AndroidKitThemeTokens.strings.back,
): Unit {
    SmallFloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = contentDescription)
    }
}

@Composable
public fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = AndroidKitThemeTokens.strings.add,
): Unit {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Default.Add, contentDescription = contentDescription)
    }
}

@Composable
public fun FloatingActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = dimensions.spaceExtraSmall,
        shadowElevation = dimensions.spaceSmall,
    ) {
        Row(
            modifier = Modifier.padding(dimensions.spaceSmall),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
            content = content,
        )
    }
}
