package io.github.octaviusframework.driver.mapping.result

import io.github.octaviusframework.driver.type.PgType
import kotlin.reflect.KType

interface ResultConverter<T : Any> {
    fun canConvert(source: Any, expectedType: KType, sourceType: PgType): Boolean
    fun convert(source: Any, expectedType: KType, context: DeserializationContext, sourceType: PgType): T
}
