package io.github.octaviusframework.driver.type

data class CompositeRegistration(
    val qualifiedName: QualifiedName,
    val pgConvention: CaseConvention,
    val kotlinConvention: CaseConvention
)
