package xyz.demorgan.toolwindow

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class ConverterRowsTest : LightJavaCodeInsightFixtureTestCase() {

    fun testBuildsRowsForConverters() {
        myFixture.addClass(
            "package org.springframework.core.convert.converter; " +
                "public interface Converter<S, T> { T convert(S source); }",
        )
        myFixture.addClass("package model; public class Foo {}")
        myFixture.addClass("package model; public class Bar {}")
        myFixture.addClass("package c; public class FooToBarConverter {}")
        myFixture.addClass(
            "package c; " +
                "import org.springframework.core.convert.converter.Converter; " +
                "import model.Foo; import model.Bar; " +
                "public class SpringConv implements Converter<Foo, Bar> { public Bar convert(Foo s) { return null; } }",
        )

        val labels = ConverterRows.build(project).map { it.label }

        assertTrue("expected name converter, got $labels", labels.any { it.contains("FooToBarConverter") })
        assertTrue("expected spring converter, got $labels", labels.any { it.contains("SpringConv") })
    }

    fun testEmptyWhenNoConverters() {
        myFixture.addClass("package c; public class JustAService {}")
        assertEmpty(ConverterRows.build(project))
    }
}
