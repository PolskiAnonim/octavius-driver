package io.github.octaviusframework.serialization

import io.github.octaviusframework.types.TypeRegistry

class ParameterConverterRegistry {
    private val converters = mutableListOf<ParameterConverter<*>>()

    fun addConverter(converter: ParameterConverter<*>) {
        converters.add(converter)
    }

    fun convert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Any? {
        val converter = converters.firstOrNull { it.canConvert(source, expectedOid, typeRegistry) }
        return converter?.convert(source, expectedOid, typeRegistry) ?: source
    }
}
