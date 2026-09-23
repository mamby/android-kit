package net.mamby.androidkit.compose.action

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
public sealed interface AndroidKitActionItem

@Immutable
public class AndroidKitAction(
    public val icon: ImageVector,
    public val label: String,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
) : AndroidKitActionItem

/** An action rendered as text when shown directly. */
@Immutable
public class AndroidKitTextAction(
    public val label: String,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
) : AndroidKitActionItem

/** An action rendered with a horizontal icon and label when shown directly. */
@Immutable
public class AndroidKitIconAndLabelAction(
    public val icon: ImageVector,
    public val label: String,
    public val onClick: () -> Unit,
    public val enabled: Boolean = true,
) : AndroidKitActionItem

@Immutable
public data object AndroidKitActionSeparator : AndroidKitActionItem

internal val AndroidKitActionItem.isAndroidKitAction: Boolean
    get() = when (this) {
        is AndroidKitAction,
        is AndroidKitTextAction,
        is AndroidKitIconAndLabelAction,
        -> true

        AndroidKitActionSeparator -> false
    }

internal data class PartitionedAndroidKitActions(
    val direct: List<AndroidKitActionItem>,
    val overflow: List<AndroidKitActionItem>,
)

internal fun partitionAndroidKitActions(
    items: List<AndroidKitActionItem>,
    directActionCount: Int,
): PartitionedAndroidKitActions {
    var splitIndex = 0
    var directActionsRemaining = directActionCount
    while (splitIndex < items.size && directActionsRemaining > 0) {
        if (items[splitIndex].isAndroidKitAction) directActionsRemaining -= 1
        splitIndex += 1
    }

    return PartitionedAndroidKitActions(
        direct = items.subList(0, splitIndex).normalizedAndroidKitActions(),
        overflow = items.subList(splitIndex, items.size).normalizedAndroidKitActions(),
    )
}

internal fun List<AndroidKitActionItem>.normalizedAndroidKitActions():
    List<AndroidKitActionItem> = buildList {
        this@normalizedAndroidKitActions.forEach { item ->
            when (item) {
                is AndroidKitAction,
                is AndroidKitTextAction,
                is AndroidKitIconAndLabelAction,
                -> add(item)

                AndroidKitActionSeparator -> {
                    if (isNotEmpty() && last() !== AndroidKitActionSeparator) add(item)
                }
            }
        }
        if (lastOrNull() === AndroidKitActionSeparator) removeAt(lastIndex)
    }

internal const val MaximumDirectHeaderActions: Int = 2
