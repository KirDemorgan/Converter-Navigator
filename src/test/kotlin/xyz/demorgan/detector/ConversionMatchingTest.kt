package xyz.demorgan.detector

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversionMatchingTest {

    @Test
    fun orderGenericsSourceFirst() {
        assertEquals("A" to "B", ConversionMatching.orderGenerics("A", "B", true))
    }

    @Test
    fun orderGenericsSourceSecond() {
        assertEquals("B" to "A", ConversionMatching.orderGenerics("A", "B", false))
    }

    @Test
    fun nameConversionMatchesBySimpleNameBothEnds() {
        val conversion = Conversion("Foo", "Bar", RuleKind.NAME)
        assertTrue(ConversionMatching.matchesType(conversion, typeFqn = null, typeSimpleName = "Foo"))
        assertTrue(ConversionMatching.matchesType(conversion, typeFqn = null, typeSimpleName = "Bar"))
        assertFalse(ConversionMatching.matchesType(conversion, typeFqn = null, typeSimpleName = "Baz"))
    }

    @Test
    fun nameConversionRequiresSimpleName() {
        val conversion = Conversion("Foo", "Bar", RuleKind.NAME)
        assertFalse(ConversionMatching.matchesType(conversion, typeFqn = "a.Foo", typeSimpleName = null))
    }

    @Test
    fun interfaceConversionMatchesByFqn() {
        val conversion = Conversion("a.Foo", "a.Bar", RuleKind.INTERFACE)
        assertTrue(ConversionMatching.matchesType(conversion, typeFqn = "a.Foo", typeSimpleName = "Foo"))
        assertTrue(ConversionMatching.matchesType(conversion, typeFqn = "a.Bar", typeSimpleName = "Bar"))
        assertFalse(ConversionMatching.matchesType(conversion, typeFqn = "a.Baz", typeSimpleName = "Baz"))
    }

    @Test
    fun annotationConversionDoesNotMatchBySimpleName() {
        val conversion = Conversion("a.Foo", "a.Bar", RuleKind.ANNOTATION)
        assertFalse(ConversionMatching.matchesType(conversion, typeFqn = null, typeSimpleName = "Foo"))
    }

    @Test
    fun ignoresJavaLangTypes() {
        assertTrue(ConversionMatching.isIgnoredType("java.lang.String"))
        assertFalse(ConversionMatching.isIgnoredType("com.acme.User"))
    }
}
