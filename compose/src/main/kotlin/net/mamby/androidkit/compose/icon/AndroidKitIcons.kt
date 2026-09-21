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
    val Search: ImageVector = settingsIcon("Search", "M21 21L16.65 16.65 M19 11A8 8 0 1 1 3 11A8 8 0 1 1 19 11")
    val Microphone: ImageVector = settingsIcon("Mic", "M9 5A3 3 0 0 1 15 5V12A3 3 0 0 1 9 12Z M5 10V12A7 7 0 0 0 19 12V10 M12 19V22")
    val Contact: ImageVector = settingsIcon("Mail", "M4 4H20A2 2 0 0 1 22 6V18A2 2 0 0 1 20 20H4A2 2 0 0 1 2 18V6A2 2 0 0 1 4 4 M22 6L12 13L2 6")
    val Info: ImageVector = settingsIcon("Info", "M22 12A10 10 0 1 1 2 12A10 10 0 1 1 22 12 M12 16V12 M12 8H12.01")
    val Document: ImageVector = settingsIcon("FileText", "M14 2H6A2 2 0 0 0 4 4V20A2 2 0 0 0 6 22H18A2 2 0 0 0 20 20V8Z M14 2V8H20 M8 13H16 M8 17H16")
    val Website: ImageVector = settingsIcon("Globe", "M22 12A10 10 0 1 1 2 12A10 10 0 1 1 22 12 M2 12H22 M12 2C16 6 16 18 12 22C8 18 8 6 12 2")
    val Code: ImageVector = settingsIcon("Code", "M16 18L22 12L16 6 M8 6L2 12L8 18")
    val Copy: ImageVector = settingsIcon(
        "Copy",
        "M8 8H20A2 2 0 0 1 22 10V20A2 2 0 0 1 20 22H10A2 2 0 0 1 8 20Z " +
            "M16 8V4A2 2 0 0 0 14 2H4A2 2 0 0 0 2 4V14A2 2 0 0 0 4 16H8",
    )
    val Contributors: ImageVector = settingsIcon("Users", "M16 21V19A4 4 0 0 0 12 15H6A4 4 0 0 0 2 19V21 M13 7A4 4 0 1 1 5 7A4 4 0 1 1 13 7 M22 21V19A4 4 0 0 0 19 15.13 M16 3.13A4 4 0 0 1 16 10.87")

    private fun settingsIcon(name: String, data: String): ImageVector = lucideIcon(name = name) {
        addPath(pathData = addPathNodes(data), fill = null, stroke = LucideStroke,
            strokeLineWidth = LucideStrokeWidth, strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round)
    }

    // Lucide heart, under the same ISC license as the other Kit chrome icons.
    val Support: ImageVector = lucideIcon(name = "Heart") {
        addPath(
            pathData = addPathNodes(
                "M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5" +
                    "c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5",
            ),
            fill = null, stroke = LucideStroke, strokeLineWidth = LucideStrokeWidth,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
    }

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
