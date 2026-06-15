package xyz.demorgan.toolwindow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterRowFilterTest {

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(ConverterRowFilter.matchesText("usertouserdtoconverter user userdto", "   "))
    }

    @Test
    fun matchesBySubstringCaseInsensitive() {
        val text = "usertouserdtoconverter user userdto"
        assertTrue(ConverterRowFilter.matchesText(text, "UserDto"))
        assertTrue(ConverterRowFilter.matchesText(text, "converter"))
        assertFalse(ConverterRowFilter.matchesText(text, "order"))
    }

    @Test
    fun matchesByOwnerAndType() {
        val text = "todto ordermapper order orderdto"
        assertTrue(ConverterRowFilter.matchesText(text, "mapper"))
        assertTrue(ConverterRowFilter.matchesText(text, "orderdto"))
    }
}
