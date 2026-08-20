package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun PageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
        ),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, maxLines = 1)
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                },
                navigationIcon = { navigationIcon?.invoke() },
                actions = actions,
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

@Composable
public fun AdaptiveGridPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    minimumCardWidth: Dp = AndroidKitThemeTokens.dimensions.cardMinWidth,
    content: LazyGridScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minimumCardWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimensions.screenPadding,
            top = contentPadding.calculateTopPadding() + dimensions.spaceSmall,
            end = dimensions.screenPadding,
            bottom = contentPadding.calculateBottomPadding() + dimensions.spaceExtraLarge,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        content = content,
    )
}

@Composable
public fun DetailPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    maxWidth: Dp = AndroidKitThemeTokens.dimensions.detailMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .verticalScroll(rememberScrollState())
                .padding(dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
            content = content,
        )
    }
}

@Composable
public fun PageFloatingAction(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
): Unit {
    FloatingActionButton(onClick = onClick, content = content)
}
