package xyz.demorgan.detector

object ConversionMatching {

    fun orderGenerics(first: String, second: String, sourceFirst: Boolean): Pair<String, String> =
        if (sourceFirst) first to second else second to first

    fun matchesType(
        conversion: Conversion,
        typeFqn: String?,
        typeSimpleName: String?,
    ): Boolean = when (conversion.kind) {
        RuleKind.NAME ->
            typeSimpleName != null && (conversion.fromType == typeSimpleName || conversion.toType == typeSimpleName)
        RuleKind.INTERFACE, RuleKind.ANNOTATION ->
            typeFqn != null && (conversion.fromType == typeFqn || conversion.toType == typeFqn)
    }

    fun isIgnoredType(fqn: String): Boolean = fqn.startsWith("java.lang.")
}
