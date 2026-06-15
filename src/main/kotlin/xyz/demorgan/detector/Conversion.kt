package xyz.demorgan.detector

enum class RuleKind { NAME, INTERFACE, ANNOTATION }

data class Conversion(
    val fromType: String,
    val toType: String,
    val kind: RuleKind,
)
