package xyz.demorgan.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.pom.Navigatable
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import xyz.demorgan.ConverterIcons
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel

class ConverterToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val model = DefaultListModel<ConverterRow>()
    private val list = JBList(model)

    init {
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = object : ColoredListCellRenderer<ConverterRow>() {
            override fun customizeCellRenderer(
                list: JList<out ConverterRow>,
                value: ConverterRow,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean,
            ) {
                icon = ConverterIcons.CONVERTER
                append(value.label)
            }
        }
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                navigateToSelection()
                return true
            }
        }.installOn(list)
        setContent(ScrollPaneFactory.createScrollPane(list))
        toolbar = createToolbar()
        refresh()
    }

    private fun createToolbar(): JComponent {
        val group = DefaultActionGroup()
        group.add(object : AnAction("Refresh", "Reload converters", AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) = refresh()
        })
        val toolbar = ActionManager.getInstance().createActionToolbar("ConverterNavigatorToolWindow", group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    fun refresh() {
        model.clear()
        ConverterRows.build(project).forEach { model.addElement(it) }
    }

    private fun navigateToSelection() {
        val target = list.selectedValue?.target as? Navigatable ?: return
        if (target.canNavigate()) target.navigate(true)
    }
}
