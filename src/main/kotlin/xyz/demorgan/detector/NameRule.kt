package xyz.demorgan.detector

import java.util.regex.Pattern

class NameRule(patterns: List<String>) {

    private val compiled: List<Pattern> =
        patterns.mapNotNull { runCatching { Pattern.compile(it) }.getOrNull() }

    fun match(className: String): Pair<String, String>? {
        for (pattern in compiled) {
            val matcher = pattern.matcher(className)
            if (matcher.matches() && matcher.groupCount() >= 2) {
                return matcher.group(1) to matcher.group(2)
            }
        }
        return null
    }

    fun matches(className: String): Boolean = match(className) != null
}
