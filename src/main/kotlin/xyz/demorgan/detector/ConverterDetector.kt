package xyz.demorgan.detector

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import xyz.demorgan.settings.ConverterSettings

class ConverterDetector(private val settings: ConverterSettings = ConverterSettings.getInstance()) {

    private val nameRule = NameRule(settings.namePatterns)

    fun detect(psiClass: PsiClass): List<DetectedConversion> {
        val result = mutableListOf<DetectedConversion>()
        detectByName(psiClass)?.let { result.add(it) }
        result.addAll(detectByInterface(psiClass))
        result.addAll(detectByAnnotation(psiClass))
        return result
    }

    private fun detectByName(psiClass: PsiClass): DetectedConversion? {
        val name = psiClass.name ?: return null
        val (from, to) = nameRule.match(name) ?: return null
        return DetectedConversion(psiClass.navigationElement, from, to, RuleKind.NAME)
    }

    private fun detectByInterface(psiClass: PsiClass): List<DetectedConversion> {
        if (settings.interfaceFqns.isEmpty()) return emptyList()
        val out = mutableListOf<DetectedConversion>()
        for (superType in psiClass.superTypes) {
            val resolved = superType.resolve() ?: continue
            val fqn = resolved.qualifiedName ?: continue
            if (fqn !in settings.interfaceFqns) continue
            val args = superType.parameters
            if (args.size < 2) continue
            val first = typeFqn(args[0]) ?: continue
            val second = typeFqn(args[1]) ?: continue
            val (from, to) = ConversionMatching.orderGenerics(first, second, settings.interfaceSourceFirst)
            out.add(DetectedConversion(psiClass.navigationElement, from, to, RuleKind.INTERFACE))
        }
        return out
    }

    private fun detectByAnnotation(psiClass: PsiClass): List<DetectedConversion> {
        if (settings.annotationFqns.isEmpty()) return emptyList()
        val isMapper = settings.annotationFqns.any { psiClass.hasAnnotation(it) }
        if (!isMapper) return emptyList()
        val out = mutableListOf<DetectedConversion>()
        for (method in psiClass.methods) {
            val conversion = mappingMethod(method) ?: continue
            out.add(conversion)
        }
        return out
    }

    private fun mappingMethod(method: PsiMethod): DetectedConversion? {
        if (method.isConstructor) return null
        if (method.hasModifierProperty(PsiModifier.STATIC)) return null
        val returnType = method.returnType ?: return null
        if (returnType.canonicalText == "void") return null
        val params = method.parameterList.parameters
        if (params.size != 1) return null
        val from = typeFqn(params[0].type) ?: return null
        val to = typeFqn(returnType) ?: return null
        if (ConversionMatching.isIgnoredType(from) || ConversionMatching.isIgnoredType(to)) return null
        return DetectedConversion(method.navigationElement, from, to, RuleKind.ANNOTATION)
    }

    private fun typeFqn(type: PsiType): String? {
        val classType = type as? PsiClassType ?: return null
        return classType.resolve()?.qualifiedName ?: classType.canonicalText
    }
}
