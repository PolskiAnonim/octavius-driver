package io.github.octaviusframework.driver.mapping.parameter

import io.github.octaviusframework.driver.type.TypeRegistry

class ParameterConverterRegistry(
    private val parent: ParameterConverterRegistry? = null
) {
    private val converters = mutableListOf<ParameterConverter<*>>()

    fun addConverter(converter: ParameterConverter<*>) {
        converters.add(0, converter)
    }

    fun convert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Any? {
        val converter = converters.firstOrNull { it.canConvert(source, expectedOid, typeRegistry) }
        val result = converter?.convert(source, expectedOid, typeRegistry)
        if (result != null) return result
        
        return parent?.convert(source, expectedOid, typeRegistry) ?: source
    }
}
