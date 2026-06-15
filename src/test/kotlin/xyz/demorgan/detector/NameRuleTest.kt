package xyz.demorgan.detector

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NameRuleTest {

    private val rule = NameRule(listOf("^(\\w+)To(\\w+)(Converter|Mapper)$"))

    @Test
    fun matchesConverterSuffix() {
        assertEquals("Foo" to "Bar", rule.match("FooToBarConverter"))
    }

    @Test
    fun matchesMapperSuffix() {
        assertEquals("Foo" to "Bar", rule.match("FooToBarMapper"))
    }

    @Test
    fun extractsComplexTypeNames() {
        assertEquals("User" to "UserDto", rule.match("UserToUserDtoConverter"))
    }

    @Test
    fun rejectsNameWithoutToSeparator() {
        assertNull(rule.match("FooBarConverter"))
    }

    @Test
    fun rejectsUnknownSuffix() {
        assertNull(rule.match("FooToBarService"))
    }

    @Test
    fun matchesFlag() {
        assertTrue(rule.matches("FooToBarConverter"))
        assertFalse(rule.matches("PlainClass"))
    }

    @Test
    fun ignoresInvalidPatternsGracefully() {
        val withBroken = NameRule(listOf("(((", "^(\\w+)To(\\w+)Converter$"))
        assertEquals("A" to "B", withBroken.match("AToBConverter"))
    }
}
