package xyz.demorgan.settings

object SettingsTextCodec {

    fun linesToList(text: String): List<String> =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }

    fun listToText(list: List<String>): String = list.joinToString("\n")
}
