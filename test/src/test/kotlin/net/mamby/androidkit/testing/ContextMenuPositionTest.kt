package net.mamby.androidkit.testing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import net.mamby.androidkit.compose.action.ContextMenuPositionProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextMenuPositionTest {
    @Test
    fun localPointerCoordinatesTranslateToWindowCoordinatesInBothDirections() {
        val provider = ContextMenuPositionProvider(Offset(200f, 50f), Density(1f))
        val anchor = IntRect(100, 200, 400, 600)
        val window = IntSize(1000, 1000)
        val menu = IntSize(200, 100)
        assertEquals(IntOffset(300, 250), provider.calculatePosition(anchor, window, LayoutDirection.Ltr, menu))
        assertEquals(IntOffset(100, 250), provider.calculatePosition(anchor, window, LayoutDirection.Rtl, menu))
    }

    @Test
    fun bottomRightInvocationFallsBackAboveAndToTheLeft() {
        val provider = ContextMenuPositionProvider(Offset(40f, 40f), Density(1f))
        assertEquals(
            IntOffset(740, 840),
            provider.calculatePosition(
                IntRect(900, 900, 990, 990), IntSize(1000, 1000),
                LayoutDirection.Ltr, IntSize(200, 100),
            ),
        )
    }

    @Test
    fun invocationWithoutAPointerUsesContentBounds() {
        val provider = ContextMenuPositionProvider(null, Density(1f))
        assertEquals(
            IntOffset(100, 600),
            provider.calculatePosition(
                IntRect(100, 200, 400, 600), IntSize(1000, 1000),
                LayoutDirection.Ltr, IntSize(200, 100),
            ),
        )
    }
}
