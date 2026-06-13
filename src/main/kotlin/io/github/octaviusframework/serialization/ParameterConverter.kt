package io.github.octaviusframework.serialization

import io.github.octaviusframework.types.TypeRegistry

interface ParameterConverter<T : Any> {
    fun canConvert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Boolean
    fun convert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Any?
}
