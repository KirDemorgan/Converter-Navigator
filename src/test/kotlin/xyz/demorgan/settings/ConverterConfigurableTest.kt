package xyz.demorgan.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ConverterConfigurableTest : BasePlatformTestCase() {

    fun testApplyAndResetLifecycle() {
        val settings = ConverterSettings()
        val configurable = ConverterConfigurable(settings)
        configurable.createComponent()
        assertFalse("freshly reset form must not be modified", configurable.isModified)

        settings.replaceAll(emptyList(), emptyList(), emptyList(), false)
        assertTrue("settings changed under the form — must be modified", configurable.isModified)

        configurable.apply()
        assertFalse(configurable.isModified)
        assertEquals(listOf(ConverterSettings.DEFAULT_NAME_PATTERN), settings.namePatterns)
        assertEquals(listOf(ConverterSettings.DEFAULT_INTERFACE), settings.interfaceFqns)
        assertEquals(listOf(ConverterSettings.DEFAULT_ANNOTATION), settings.annotationFqns)
        assertTrue(settings.interfaceSourceFirst)

        settings.replaceAll(listOf("X"), listOf("Y"), listOf("Z"), false)
        configurable.reset()
        assertFalse("reset must sync the form back to settings", configurable.isModified)
    }
}
