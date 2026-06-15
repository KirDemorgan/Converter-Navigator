package xyz.demorgan.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.pom.Navigatable
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent

class ConverterToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val model = DefaultListModel<ConverterRow>()
    private val list = JBList(model)
    private val searchField = SearchTextField()
    private val countLabel = JBLabel()
    private var allRows: List<ConverterRow> = emptyList()

    init {
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = ConverterCellRenderer()
        list.emptyText.text = "No converters found"

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                navigateToSelection()
                return true
            }
        }.installOn(list)

        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) navigateToSelection()
            }
        })

        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = applyFilter()
        })

        countLabel.foreground = UIUtil.getContextHelpForeground()
        countLabel.border = JBUI.Borders.empty(4, 8)

        val center = JPanel(BorderLayout())
        center.add(searchField, BorderLayout.NORTH)
        center.add(ScrollPaneFactory.createScrollPane(list), BorderLayout.CENTER)
        center.add(countLabel, BorderLayout.SOUTH)
        setContent(center)
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
        allRows = ConverterRows.build(project)
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = ConverterRowFilter.filter(allRows, searchField.text)
        model.clear()
        filtered.forEach { model.addElement(it) }
        val typeCount = allRows.flatMap { listOf(it.fromType, it.toType) }.toSet().size
        countLabel.text = "${allRows.size} converters · $typeCount types"
    }

    private fun navigateToSelection() {
        val target = list.selectedValue?.target as? Navigatable ?: return
        if (target.canNavigate()) target.navigate(true)
    }
}
