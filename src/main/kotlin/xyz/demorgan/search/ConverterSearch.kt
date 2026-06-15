package xyz.demorgan.search

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import xyz.demorgan.detector.ConversionMatching
import xyz.demorgan.detector.ConverterDetector
import xyz.demorgan.detector.DetectedConversion
import xyz.demorgan.detector.NameRule
import xyz.demorgan.settings.ConverterSettings

@Service(Service.Level.PROJECT)
class ConverterSearch(private val project: Project) {

    fun allConversions(): List<DetectedConversion> =
        CachedValuesManager.getManager(project).getCachedValue(project) {
            CachedValueProvider.Result.create(
                computeAll(),
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }

    fun conversionsForType(type: PsiClass): List<DetectedConversion> {
        val fqn = type.qualifiedName
        val simple = type.name
        return allConversions().filter { ConversionMatching.matchesType(it.asConversion(), fqn, simple) }
    }

    private fun computeAll(): List<DetectedConversion> {
        val settings = ConverterSettings.getInstance()
        val detector = ConverterDetector(settings)
        val scope = GlobalSearchScope.projectScope(project)
        val candidates = LinkedHashSet<PsiClass>()

        collectByName(settings, scope, candidates)
        collectByInterface(settings, scope, candidates)
        collectByAnnotation(settings, scope, candidates)

        val result = mutableListOf<DetectedConversion>()
        for (candidate in candidates) {
            result.addAll(detector.detect(candidate))
        }
        return result
    }

    private fun collectByName(
        settings: ConverterSettings,
        scope: GlobalSearchScope,
        out: MutableSet<PsiClass>,
    ) {
        if (settings.namePatterns.isEmpty()) return
        val nameRule = NameRule(settings.namePatterns)
        val cache = PsiShortNamesCache.getInstance(project)
        for (name in cache.allClassNames) {
            if (!nameRule.matches(name)) continue
            out.addAll(cache.getClassesByName(name, scope))
        }
    }

    private fun collectByInterface(
        settings: ConverterSettings,
        scope: GlobalSearchScope,
        out: MutableSet<PsiClass>,
    ) {
        val facade = JavaPsiFacade.getInstance(project)
        for (fqn in settings.interfaceFqns) {
            val iface = facade.findClass(fqn, GlobalSearchScope.allScope(project)) ?: continue
            out.addAll(ClassInheritorsSearch.search(iface, scope, true).findAll())
        }
    }

    private fun collectByAnnotation(
        settings: ConverterSettings,
        scope: GlobalSearchScope,
        out: MutableSet<PsiClass>,
    ) {
        val facade = JavaPsiFacade.getInstance(project)
        for (fqn in settings.annotationFqns) {
            val annotation = facade.findClass(fqn, GlobalSearchScope.allScope(project)) ?: continue
            out.addAll(AnnotatedElementsSearch.searchPsiClasses(annotation, scope).findAll())
        }
    }

    companion object {
        fun getInstance(project: Project): ConverterSearch = project.getService(ConverterSearch::class.java)
    }
}
