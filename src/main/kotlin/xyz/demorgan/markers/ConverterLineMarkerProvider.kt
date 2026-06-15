package xyz.demorgan.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElement
import xyz.demorgan.ConverterIcons
import xyz.demorgan.detector.ConverterDetector
import xyz.demorgan.detector.DetectedConversion
import xyz.demorgan.detector.RuleKind
import xyz.demorgan.search.ConverterSearch

class ConverterLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        val owner = element.parent as? PsiNameIdentifierOwner ?: return
        if (owner.nameIdentifier !== element) return
        val uClass = owner.toUElement() as? UClass ?: return
        val psiClass = uClass.javaPsi

        markConverterClass(psiClass, element, result)
        markConvertedType(psiClass, element, result)
    }

    private fun markConverterClass(
        psiClass: PsiClass,
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        val own = ConverterDetector().detect(psiClass)
        if (own.isEmpty()) return
        val targets = own.flatMap { resolveTypes(psiClass, it) }.distinct()
        if (targets.isEmpty()) return
        result.add(
            NavigationGutterIconBuilder.create(ConverterIcons.CONVERTER)
                .setTargets(targets)
                .setTooltipText("Converter — go to converted types")
                .createLineMarkerInfo(anchor),
        )
    }

    private fun markConvertedType(
        psiClass: PsiClass,
        anchor: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        val converters = ConverterSearch.getInstance(psiClass.project).conversionsForType(psiClass)
        val targets = converters.map { it.target }.distinct()
        if (targets.isEmpty()) return
        result.add(
            NavigationGutterIconBuilder.create(ConverterIcons.CONVERTER)
                .setTargets(targets)
                .setTooltipText("Converters for ${psiClass.name}")
                .createLineMarkerInfo(anchor),
        )
    }

    private fun resolveTypes(context: PsiClass, conversion: DetectedConversion): List<PsiClass> {
        return resolveType(context, conversion.fromType, conversion.kind) +
            resolveType(context, conversion.toType, conversion.kind)
    }

    private fun resolveType(context: PsiClass, type: String, kind: RuleKind): List<PsiClass> {
        val project = context.project
        val scope = GlobalSearchScope.allScope(project)
        return when (kind) {
            RuleKind.NAME -> PsiShortNamesCache.getInstance(project).getClassesByName(type, scope).toList()
            else -> listOfNotNull(JavaPsiFacade.getInstance(project).findClass(type, scope))
        }
    }
}
