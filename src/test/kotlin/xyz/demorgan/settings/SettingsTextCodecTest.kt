package xyz.demorgan.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTextCodecTest {

    @Test
    fun linesToListTrimsAndDropsBlanks() {
        assertEquals(listOf("a", "b"), SettingsTextCodec.linesToList("  a \n\n b \n   "))
    }

    @Test
    fun listToTextJoinsWithNewlines() {
        assertEquals("a\nb", SettingsTextCodec.listToText(listOf("a", "b")))
    }

    @Test
    fun roundTripPreservesEntries() {
        val list = listOf("one", "two", "three")
        assertEquals(list, SettingsTextCodec.linesToList(SettingsTextCodec.listToText(list)))
    }

    @Test
    fun blankTextYieldsEmptyList() {
        assertTrue(SettingsTextCodec.linesToList("   \n  ").isEmpty())
    }
}
