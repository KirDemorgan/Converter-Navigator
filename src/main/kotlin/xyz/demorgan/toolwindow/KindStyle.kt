package xyz.demorgan.toolwindow

import com.intellij.ui.JBColor
import xyz.demorgan.detector.RuleKind

object KindStyle {

    data class Style(val label: String, val background: JBColor, val foreground: JBColor)

    fun of(kind: RuleKind): Style = when (kind) {
        RuleKind.NAME -> Style("name", JBColor(0xE6F1FB, 0x21364A), JBColor(0x185FA5, 0x85B7EB))
        RuleKind.INTERFACE -> Style("interface", JBColor(0xE1F5EE, 0x16352C), JBColor(0x0F6E56, 0x5DCAA5))
        RuleKind.ANNOTATION -> Style("annotation", JBColor(0xEEEDFE, 0x2A2547), JBColor(0x534AB7, 0xAFA9EC))
    }
}
