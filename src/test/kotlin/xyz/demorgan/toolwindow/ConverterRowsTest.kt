package xyz.demorgan.toolwindow

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import xyz.demorgan.detector.RuleKind

class ConverterRowsTest : LightJavaCodeInsightFixtureTestCase() {

    private fun addConverters() {
        myFixture.addClass(
            "package org.springframework.core.convert.converter; " +
                "public interface Converter<S, T> { T convert(S source); }",
        )
        myFixture.addClass("package org.mapstruct; public @interface Mapper {}")
        myFixture.addClass("package model; public class Order {}")
        myFixture.addClass("package model; public class OrderDto {}")
        myFixture.addClass("package c; public class UserToUserDtoConverter {}")
        myFixture.addClass(
            "package c; import org.springframework.core.convert.converter.Converter; " +
                "import model.Order; import model.OrderDto; " +
                "public class OrderSpringConverter implements Converter<Order, OrderDto> { " +
                "public OrderDto convert(Order s) { return null; } }",
        )
        myFixture.addClass(
            "package c; import org.mapstruct.Mapper; import model.Order; import model.OrderDto; " +
                "@Mapper public interface OrderMapper { OrderDto toDto(Order o); }",
        )
    }

    fun testRowsCarryStructuredData() {
        addConverters()
        val rows = ConverterRows.build(project)

        val mapper = rows.single { it.kind == RuleKind.ANNOTATION }
        assertEquals("toDto", mapper.name)
        assertEquals("OrderMapper", mapper.owner)
        assertEquals("Order", mapper.fromType)
        assertEquals("OrderDto", mapper.toType)

        val spring = rows.single { it.kind == RuleKind.INTERFACE }
        assertEquals("OrderSpringConverter", spring.name)
        assertNull(spring.owner)
    }

    fun testRowsSortedByKindThenName() {
        addConverters()
        val kinds = ConverterRows.build(project).map { it.kind }
        assertEquals(listOf(RuleKind.NAME, RuleKind.INTERFACE, RuleKind.ANNOTATION), kinds)
    }

    fun testFilterByTypeName() {
        addConverters()
        val rows = ConverterRows.build(project)
        val filtered = ConverterRowFilter.filter(rows, "userdto")
        assertEquals(1, filtered.size)
        assertEquals("UserToUserDtoConverter", filtered.single().name)
    }

    fun testEmptyWhenNoConverters() {
        myFixture.addClass("package c; public class JustAService {}")
        assertEmpty(ConverterRows.build(project))
    }
}
