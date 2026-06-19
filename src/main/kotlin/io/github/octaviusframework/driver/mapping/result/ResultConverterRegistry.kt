package io.github.octaviusframework.driver.mapping.result

import io.github.octaviusframework.driver.type.PgType
import kotlin.reflect.KType

class ResultConverterRegistry(
    private val parent: ResultConverterRegistry? = null
) {
    private val converters = mutableListOf<ResultConverter<*>>()

    fun addConverter(converter: ResultConverter<*>) {
        // Dodawanie na początek, aby nowsze konwertery miały wyższy priorytet
        converters.add(0, converter)
    }

    fun findConverter(source: Any, expectedType: KType, sourceType: PgType): ResultConverter<*>? {
        val converter = converters.find { it.canConvert(source, expectedType, sourceType) }
        return converter ?: parent?.findConverter(source, expectedType, sourceType)
    }
}