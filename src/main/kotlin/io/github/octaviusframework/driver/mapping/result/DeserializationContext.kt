package io.github.octaviusframework.driver.mapping.result

import io.github.octaviusframework.driver.type.PgType
import kotlin.reflect.KType

interface DeserializationContext {
    fun <T> convert(source: Any?, expectedType: KType, sourceType: PgType): T
}
