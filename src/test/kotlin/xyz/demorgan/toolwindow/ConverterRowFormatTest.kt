package xyz.demorgan.toolwindow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.demorgan.detector.RuleKind

class ConverterRowFormatTest {

    @Test
    fun shortNameStripsPackage() {
        assertEquals("Foo", ConverterRowFormat.shortName("model.dto.Foo"))
        assertEquals("Foo", ConverterRowFormat.shortName("Foo"))
    }

    @Test
    fun labelContainsNameAndTypes() {
        val label = ConverterRowFormat.formatLabel("FooToBarConverter", "model.Foo", "model.Bar", RuleKind.NAME)
        assertTrue(label, label.contains("FooToBarConverter"))
        assertTrue(label, label.contains("Foo → Bar"))
        assertTrue(label, label.contains("name"))
    }

    @Test
    fun anonymousWhenNoName() {
        val label = ConverterRowFormat.formatLabel(null, "a.Foo", "a.Bar", RuleKind.INTERFACE)
        assertTrue(label, label.contains("<anonymous>"))
    }
}
