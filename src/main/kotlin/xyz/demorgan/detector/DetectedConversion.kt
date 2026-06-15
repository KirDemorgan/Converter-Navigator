package xyz.demorgan.detector

import com.intellij.psi.PsiElement

data class DetectedConversion(
    val target: PsiElement,
    val fromType: String,
    val toType: String,
    val kind: RuleKind,
)
