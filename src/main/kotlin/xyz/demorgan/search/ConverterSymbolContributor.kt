package xyz.demorgan.search

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

class ConverterSymbolContributor : ChooseByNameContributorEx {

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val project = scope.project ?: return
        ConverterSearch.getInstance(project).converterTargets()
            .mapNotNull { (it as? PsiNamedElement)?.name }
            .distinct()
            .forEach { if (!processor.process(it)) return }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        val project = parameters.project
        ConverterSearch.getInstance(project).converterTargets()
            .filter { (it as? PsiNamedElement)?.name == name }
            .filterIsInstance<NavigationItem>()
            .forEach { if (!processor.process(it)) return }
    }
}
