package xyz.demorgan.toolwindow

import com.intellij.psi.PsiElement
import xyz.demorgan.detector.RuleKind

data class ConverterRow(
    val name: String,
    val owner: String?,
    val fromType: String,
    val toType: String,
    val kind: RuleKind,
    val target: PsiElement,
) {
    val searchText: String = buildString {
        append(name)
        owner?.let { append(' ').append(it) }
        append(' ').append(fromType).append(' ').append(toType)
    }.lowercase()
}
