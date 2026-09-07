package net.mamby.androidkit.compose.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Component chrome and predefined settings icons. Chrome and AppLock use Lucide;
 * Theme and Language preserve the shared settings artwork originally used in Fralov.
 *
 * Host-provided product icons remain outside this set.
 */
internal object AndroidKitIcons {
    val Theme: ImageVector = ImageVector.Builder(
        name = "AndroidKit.Theme", defaultWidth = 17.dp, defaultHeight = 17.dp,
        viewportWidth = 21.6f, viewportHeight = 21.6f,
    ).apply {
        addPath(
            pathData = addPathNodes(
                "M10.8,0.9 L10.8,3.5 M10.8,18.1 L10.8,20.7 M0.9,10.8 L3.5,10.8 " +
                    "M18.1,10.8 L20.7,10.8 M3.8,3.8 L5.64,5.64 M15.96,15.96 L17.8,17.8 " +
                    "M17.8,3.8 L15.96,5.64 M5.64,15.96 L3.8,17.8",
            ),
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
        addPath(
            pathData = addPathNodes(
                "M15.4,10.8 C15.4,13.34 13.34,15.4 10.8,15.4 " +
                    "C8.26,15.4 6.2,13.34 6.2,10.8 C6.2,8.26 8.26,6.2 10.8,6.2 " +
                    "C13.34,6.2 15.4,8.26 15.4,10.8 Z",
            ),
            fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
    }.build()

    val Language: ImageVector = ImageVector.Builder(
        name = "AndroidKit.Language", defaultWidth = 17.6.dp, defaultHeight = 16.dp,
        viewportWidth = 469.333f, viewportHeight = 426.667f,
    ).apply {
        group(translationY = -21.333f) {
            addPath(
                pathData = addPathNodes(
                    "M253.227,300.267L253.227,300.267L199.04,246.72l0.64,-0.64" +
                        "c37.12,-41.387 63.573,-88.96 79.147,-139.307h62.507V64H192V21.333" +
                        "h-42.667V64H0v42.453h238.293c-14.4,41.173 -36.907,80.213 -67.627,114.347" +
                        "c-19.84,-22.08 -36.267,-46.08 -49.28,-71.467H78.72" +
                        "c15.573,34.773 36.907,67.627 63.573,97.28l-108.48,107.2L64,384" +
                        "l106.667,-106.667l66.347,66.347L253.227,300.267z",
                ),
                fill = SolidColor(Color.Black),
            )
            addPath(
                pathData = addPathNodes(
                    "M373.333,192h-42.667l-96,256h42.667l24,-64h101.333l24,64h42.667" +
                        "L373.333,192zM317.333,341.333L352,248.853l34.667,92.48H317.333z",
                ),
                fill = SolidColor(Color.Black),
            )
        }
    }.build()

    val AppLock: ImageVector = lucideIcon(name = "LockKeyhole") {
        addPath(
            pathData = addPathNodes(
                "M13,16 A1,1 0,1 1,11,16 A1,1 0,1 1,13,16 Z " +
                    "M5,10 H19 A2,2 0,0 1,21,12 V20 A2,2 0,0 1,19,22 " +
                    "H5 A2,2 0,0 1,3,20 V12 A2,2 0,0 1,5,10 Z " +
                    "M7,10 V7 A5,5 0,0 1,17,7 V10",
            ),
            fill = null, stroke = LucideStroke, strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
    }

    val Check: ImageVector = lucideIcon(name = "Check") {
        path(
            fill = null, stroke = LucideStroke, strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
        }
    }

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
