package xyz.demorgan.toolwindow

object ConverterRowFormat {

    fun shortName(type: String): String = type.substringAfterLast('.')
}
