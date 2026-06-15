package xyz.demorgan.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APP)
@State(name = "ConverterNavigatorSettings", storages = [Storage("converter-navigator.xml")])
class ConverterSettings : PersistentStateComponent<ConverterSettings.State> {

    class State {
        var namePatterns: MutableList<String> = mutableListOf(DEFAULT_NAME_PATTERN)
        var interfaceFqns: MutableList<String> = mutableListOf(DEFAULT_INTERFACE)
        var annotationFqns: MutableList<String> = mutableListOf(DEFAULT_ANNOTATION)
        var interfaceSourceFirst: Boolean = true
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    val namePatterns: List<String> get() = state.namePatterns
    val interfaceFqns: List<String> get() = state.interfaceFqns
    val annotationFqns: List<String> get() = state.annotationFqns
    val interfaceSourceFirst: Boolean get() = state.interfaceSourceFirst

    companion object {
        const val DEFAULT_NAME_PATTERN = "^(\\w+)To(\\w+)(Converter|Mapper)$"
        const val DEFAULT_INTERFACE = "org.springframework.core.convert.converter.Converter"
        const val DEFAULT_ANNOTATION = "org.mapstruct.Mapper"

        fun getInstance(): ConverterSettings =
            ApplicationManager.getApplication().getService(ConverterSettings::class.java)
    }
}
