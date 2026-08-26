package net.mamby.androidkit.compose.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Component chrome icons adapted from Lucide's canonical 24-pixel SVGs.
 *
 * Host-provided product icons remain outside this set.
 */
internal object AndroidKitIcons {
    val ArrowBack: ImageVector = lucideIcon(
        name = "ArrowLeft",
        autoMirror = true,
    ) {
        path(
            fill = null,
            stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            lineTo(12f, 5f)
        }
        path(
            fill = null,
            stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(19f, 12f)
            lineTo(5f, 12f)
        }
    }

    val ChevronRight: ImageVector = lucideIcon(
        name = "ChevronRight",
        autoMirror = true,
    ) {
        path(
            fill = null,
            stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(9f, 18f)
            lineTo(15f, 12f)
            lineTo(9f, 6f)
        }
    }

    val Close: ImageVector = lucideIcon(name = "X") {
        path(
            fill = null,
            stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
        path(
            fill = null,
            stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }
    }

    val More: ImageVector = lucideIcon(name = "Ellipsis") {
        listOf(5f, 12f, 19f).forEach { centerX ->
            path(
                fill = null,
                stroke = LucideStroke,
                strokeLineWidth = LucideStrokeWidth,
            ) {
                moveTo(centerX, 11f)
                arcTo(
                    horizontalEllipseRadius = 1f,
                    verticalEllipseRadius = 1f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = centerX,
                    y1 = 13f,
                )
                arcTo(
                    horizontalEllipseRadius = 1f,
                    verticalEllipseRadius = 1f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = centerX,
                    y1 = 11f,
                )
                close()
            }
        }
    }
}

private val LucideStroke: SolidColor = SolidColor(Color.Black)
private const val LucideStrokeWidth: Float = 2f

private fun lucideIcon(
    name: String,
    autoMirror: Boolean = false,
    content: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = "Lucide.$name",
    defaultWidth = LucideIconSize,
    defaultHeight = LucideIconSize,
    viewportWidth = LucideViewportSize,
    viewportHeight = LucideViewportSize,
    autoMirror = autoMirror,
).apply(content).build()

private val LucideIconSize = 24.dp
private const val LucideViewportSize: Float = 24f
