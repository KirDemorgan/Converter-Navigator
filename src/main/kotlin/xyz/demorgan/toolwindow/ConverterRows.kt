package xyz.demorgan.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import xyz.demorgan.search.ConverterSearch

object ConverterRows {

    private data class Key(val target: PsiElement, val fromType: String, val toType: String)

    fun build(project: Project): List<ConverterRow> =
        ConverterSearch.getInstance(project).allConversions()
            .groupBy { Key(it.target, ConverterRowFormat.shortName(it.fromType), ConverterRowFormat.shortName(it.toType)) }
            .map { (key, conversions) ->
                ConverterRow(
                    name = (key.target as? PsiNamedElement)?.name ?: "<anonymous>",
                    owner = (key.target as? PsiMethod)?.containingClass?.name,
                    fromType = key.fromType,
                    toType = key.toType,
                    kinds = conversions.map { it.kind }.distinct().sortedBy { it.ordinal },
                    target = key.target,
                )
            }
            .sortedWith(compareBy({ it.kinds.first().ordinal }, { it.name.lowercase() }))
}
