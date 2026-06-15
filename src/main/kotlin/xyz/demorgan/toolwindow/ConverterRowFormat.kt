package xyz.demorgan.toolwindow

import xyz.demorgan.detector.RuleKind

object ConverterRowFormat {

    fun shortName(type: String): String = type.substringAfterLast('.')

    fun formatLabel(converterName: String?, fromType: String, toType: String, kind: RuleKind): String {
        val name = converterName ?: "<anonymous>"
        return "$name:  ${shortName(fromType)} → ${shortName(toType)}  ·  ${kind.name.lowercase()}"
    }
}
