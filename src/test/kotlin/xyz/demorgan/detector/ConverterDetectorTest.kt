package xyz.demorgan.detector

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class ConverterDetectorTest : LightJavaCodeInsightFixtureTestCase() {

    private fun addStubs() {
        myFixture.addClass(
            "package org.springframework.core.convert.converter; " +
                "public interface Converter<S, T> { T convert(S source); }",
        )
        myFixture.addClass("package org.mapstruct; public @interface Mapper {}")
        myFixture.addClass("package model; public class Foo {}")
        myFixture.addClass("package model; public class Bar {}")
    }

    fun testDetectsByName() {
        addStubs()
        val cls = myFixture.addClass("package c; public class FooToBarConverter {}")
        val conversion = ConverterDetector().detect(cls).single()
        assertEquals("Foo", conversion.fromType)
        assertEquals("Bar", conversion.toType)
        assertEquals(RuleKind.NAME, conversion.kind)
    }

    fun testDetectsBySpringInterface() {
        addStubs()
        val cls = myFixture.addClass(
            "package c; " +
                "import org.springframework.core.convert.converter.Converter; " +
                "import model.Foo; import model.Bar; " +
                "public class FooBarConv implements Converter<Foo, Bar> { " +
                "public Bar convert(Foo s) { return null; } }",
        )
        val conversion = ConverterDetector().detect(cls).single { it.kind == RuleKind.INTERFACE }
        assertEquals("model.Foo", conversion.fromType)
        assertEquals("model.Bar", conversion.toType)
    }

    fun testDetectsByMapstructAnnotation() {
        addStubs()
        val cls = myFixture.addClass(
            "package c; import org.mapstruct.Mapper; import model.Foo; import model.Bar; " +
                "@Mapper public interface FooMapper { Bar toBar(Foo f); }",
        )
        val conversion = ConverterDetector().detect(cls).single { it.kind == RuleKind.ANNOTATION }
        assertEquals("model.Foo", conversion.fromType)
        assertEquals("model.Bar", conversion.toType)
    }

    fun testPlainClassIsNotConverter() {
        addStubs()
        val cls = myFixture.addClass("package c; public class JustAService { void run() {} }")
        assertEmpty(ConverterDetector().detect(cls))
    }
}
