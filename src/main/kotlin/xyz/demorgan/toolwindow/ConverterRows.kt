package xyz.demorgan.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import xyz.demorgan.search.ConverterSearch

object ConverterRows {

    fun build(project: Project): List<ConverterRow> =
        ConverterSearch.getInstance(project).allConversions()
            .map { conversion ->
                val name = (conversion.target as? PsiNamedElement)?.name
                ConverterRow(
                    label = ConverterRowFormat.formatLabel(name, conversion.fromType, conversion.toType, conversion.kind),
                    target = conversion.target,
                )
            }
            .sortedBy { it.label }
}
