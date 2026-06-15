package xyz.demorgan.toolwindow

import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterRowFormatTest {

    @Test
    fun shortNameStripsPackage() {
        assertEquals("Foo", ConverterRowFormat.shortName("model.dto.Foo"))
        assertEquals("Foo", ConverterRowFormat.shortName("Foo"))
    }
}
