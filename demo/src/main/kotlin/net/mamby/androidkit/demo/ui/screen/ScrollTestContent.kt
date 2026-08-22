package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.demo.R

@Composable
internal fun ScrollTestContent() {
    repeat(ScrollTestSectionCount) { index ->
        ScrollTestSection(index)
    }
}

internal fun LazyGridScope.scrollTestContent() {
    items(
        count = ScrollTestSectionCount,
        span = { GridItemSpan(maxLineSpan) },
    ) { index ->
        ScrollTestSection(index)
    }
}

@Composable
private fun ScrollTestSection(index: Int) {
    SectionCard(
        title = stringResource(R.string.scroll_section_title, index + 1),
        supportingText = stringResource(R.string.scroll_section_description),
    ) {
        Text(stringResource(R.string.scroll_section_body))
    }
}

private const val ScrollTestSectionCount = 6
