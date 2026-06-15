package xyz.demorgan.toolwindow

import com.intellij.util.ui.JBUI
import xyz.demorgan.detector.RuleKind
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent

class KindBadge : JComponent() {

    private var style = KindStyle.of(RuleKind.NAME)

    init {
        isOpaque = false
        font = JBUI.Fonts.smallFont()
    }

    fun setKind(kind: RuleKind) {
        style = KindStyle.of(kind)
        revalidate()
        repaint()
    }

    override fun getPreferredSize(): Dimension {
        val metrics = getFontMetrics(font)
        return Dimension(metrics.stringWidth(style.label) + JBUI.scale(16), metrics.height + JBUI.scale(4))
    }

    override fun getMaximumSize(): Dimension = preferredSize

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val arc = height.toFloat()
            g2.color = style.background
            g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), arc, arc))
            g2.color = style.foreground
            g2.font = font
            val metrics = g2.fontMetrics
            val x = (width - metrics.stringWidth(style.label)) / 2
            val y = (height - metrics.height) / 2 + metrics.ascent
            g2.drawString(style.label, x, y)
        } finally {
            g2.dispose()
        }
    }
}
