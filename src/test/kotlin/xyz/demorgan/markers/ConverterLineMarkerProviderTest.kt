package xyz.demorgan.markers

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class ConverterLineMarkerProviderTest : LightJavaCodeInsightFixtureTestCase() {

    fun testGutterOnConverterClassPointsToTypes() {
        myFixture.addClass("package model; public class Foo {}")
        myFixture.addClass("package model; public class Bar {}")
        myFixture.configureByText("FooToBarConverter.java", "public class FooToBarConverter {}")
        myFixture.doHighlighting()

        val tooltips = myFixture.findAllGutters().map { it.tooltipText }
        assertTrue(
            "expected converter gutter, got $tooltips",
            tooltips.any { it != null && it.startsWith("Converter") },
        )
    }

    fun testGutterOnTypePointsToConverters() {
        myFixture.addClass("package c; public class FooToBarConverter {}")
        myFixture.configureByText("Foo.java", "public class Foo {}")
        myFixture.doHighlighting()

        val tooltips = myFixture.findAllGutters().map { it.tooltipText }
        assertTrue(
            "expected converters gutter for Foo, got $tooltips",
            tooltips.any { it != null && it.contains("Converters for Foo") },
        )
    }

    fun testNoGutterOnUnrelatedClass() {
        myFixture.configureByText("JustAService.java", "public class JustAService { void run() {} }")
        myFixture.doHighlighting()

        val tooltips = myFixture.findAllGutters().mapNotNull { it.tooltipText }
        assertFalse(tooltips.any { it.startsWith("Converter") || it.contains("Converters for") })
    }
}
