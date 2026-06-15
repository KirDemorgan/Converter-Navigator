package xyz.demorgan.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class ConverterConfigurable @JvmOverloads constructor(
    private val settings: ConverterSettings = ConverterSettings.getInstance(),
) : Configurable {

    private val namePatterns = JBTextArea(5, 50)
    private val interfaces = JBTextArea(4, 50)
    private val annotations = JBTextArea(3, 50)
    private val sourceFirst = JBCheckBox("First generic argument is the source type — Converter<S, T>")

    override fun getDisplayName(): String = "Converter Navigator"

    override fun createComponent(): JComponent {
        reset()
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel("Name patterns (regex with 2+ groups, one per line):"))
            .addComponent(JBScrollPane(namePatterns))
            .addComponent(JBLabel("Converter interfaces (fully qualified name, one per line):"))
            .addComponent(JBScrollPane(interfaces))
            .addComponent(JBLabel("Converter annotations (fully qualified name, one per line):"))
            .addComponent(JBScrollPane(annotations))
            .addComponent(sourceFirst)
            .addComponentFillVertically(javax.swing.JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean =
        SettingsTextCodec.linesToList(namePatterns.text) != settings.namePatterns ||
            SettingsTextCodec.linesToList(interfaces.text) != settings.interfaceFqns ||
            SettingsTextCodec.linesToList(annotations.text) != settings.annotationFqns ||
            sourceFirst.isSelected != settings.interfaceSourceFirst

    override fun apply() {
        settings.replaceAll(
            SettingsTextCodec.linesToList(namePatterns.text),
            SettingsTextCodec.linesToList(interfaces.text),
            SettingsTextCodec.linesToList(annotations.text),
            sourceFirst.isSelected,
        )
    }

    override fun reset() {
        namePatterns.text = SettingsTextCodec.listToText(settings.namePatterns)
        interfaces.text = SettingsTextCodec.listToText(settings.interfaceFqns)
        annotations.text = SettingsTextCodec.listToText(settings.annotationFqns)
        sourceFirst.isSelected = settings.interfaceSourceFirst
    }
}
