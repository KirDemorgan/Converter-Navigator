package xyz.demorgan.search

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters

class ConverterSymbolContributorTest : LightJavaCodeInsightFixtureTestCase() {

    private val contributor = ConverterSymbolContributor()

    fun testConverterNameIsContributed() {
        myFixture.addClass("package c; public class FooToBarConverter {}")

        val names = mutableListOf<String>()
        contributor.processNames(
            Processor { names.add(it); true },
            GlobalSearchScope.projectScope(project),
            null,
        )

        assertTrue("expected FooToBarConverter in $names", names.contains("FooToBarConverter"))
    }

    fun testConverterElementIsResolved() {
        myFixture.addClass("package c; public class FooToBarConverter {}")

        val items = mutableListOf<NavigationItem>()
        contributor.processElementsWithName(
            "FooToBarConverter",
            Processor { items.add(it); true },
            FindSymbolParameters.simple(project, false),
        )

        assertTrue("expected one navigation item, got $items", items.size == 1)
        assertEquals("FooToBarConverter", items.single().name)
    }

    fun testPlainClassIsNotContributed() {
        myFixture.addClass("package c; public class JustAService {}")

        val names = mutableListOf<String>()
        contributor.processNames(
            Processor { names.add(it); true },
            GlobalSearchScope.projectScope(project),
            null,
        )

        assertFalse(names.contains("JustAService"))
    }
}
