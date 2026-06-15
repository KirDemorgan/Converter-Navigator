package xyz.demorgan.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import xyz.demorgan.search.ConverterSearch

object ConverterRows {

    fun build(project: Project): List<ConverterRow> =
        ConverterSearch.getInstance(project).allConversions()
            .map { conversion ->
                val target = conversion.target
                ConverterRow(
                    name = (target as? PsiNamedElement)?.name ?: "<anonymous>",
                    owner = (target as? PsiMethod)?.containingClass?.name,
                    fromType = ConverterRowFormat.shortName(conversion.fromType),
                    toType = ConverterRowFormat.shortName(conversion.toType),
                    kind = conversion.kind,
                    target = target,
                )
            }
            .sortedWith(compareBy({ it.kind.ordinal }, { it.name.lowercase() }))
}
