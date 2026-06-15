package xyz.demorgan.toolwindow

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.util.ui.JBUI
import xyz.demorgan.ConverterIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.GridBagLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

class ConverterCellRenderer : ListCellRenderer<ConverterRow> {

    private val iconLabel = JBLabel(ConverterIcons.CONVERTER)
    private val nameLine = SimpleColoredComponent()
    private val subtitleLine = SimpleColoredComponent()
    private val badgeRow = JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(4), 0))
    private val root = JPanel(BorderLayout(JBUI.scale(8), 0))

    init {
        nameLine.isOpaque = false
        subtitleLine.isOpaque = false
        nameLine.ipad = JBUI.emptyInsets()
        subtitleLine.ipad = JBUI.emptyInsets()
        iconLabel.isOpaque = false
        badgeRow.isOpaque = false

        val text = JPanel(VerticalLayout(JBUI.scale(1)))
        text.isOpaque = false
        text.add(nameLine)
        text.add(subtitleLine)

        val badgeHolder = JPanel(GridBagLayout())
        badgeHolder.isOpaque = false
        badgeHolder.add(badgeRow)

        root.border = JBUI.Borders.empty(4, 8)
        root.add(iconLabel, BorderLayout.WEST)
        root.add(text, BorderLayout.CENTER)
        root.add(badgeHolder, BorderLayout.EAST)
    }

    override fun getListCellRendererComponent(
        list: JList<out ConverterRow>,
        value: ConverterRow,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        val background = if (isSelected) list.selectionBackground else list.background
        root.background = background
        root.isOpaque = true

        nameLine.clear()
        nameLine.append(value.name, regularAttributes(isSelected, list))
        value.owner?.let { nameLine.append("  ·  $it", dimmedAttributes(isSelected, list)) }

        subtitleLine.clear()
        subtitleLine.append("${value.fromType}  →  ${value.toType}", dimmedAttributes(isSelected, list))

        badgeRow.removeAll()
        value.kinds.forEach { kind ->
            badgeRow.add(KindBadge().apply { setKind(kind) })
        }

        return root
    }

    private fun regularAttributes(isSelected: Boolean, list: JList<*>): SimpleTextAttributes =
        if (isSelected) SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, list.selectionForeground)
        else SimpleTextAttributes.REGULAR_ATTRIBUTES

    private fun dimmedAttributes(isSelected: Boolean, list: JList<*>): SimpleTextAttributes =
        if (isSelected) SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, list.selectionForeground)
        else SimpleTextAttributes.GRAYED_ATTRIBUTES
}
