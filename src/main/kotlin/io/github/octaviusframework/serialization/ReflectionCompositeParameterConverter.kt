package io.github.octaviusframework.serialization

import io.github.octaviusframework.container.ContainerField
import io.github.octaviusframework.container.PgComposite
import io.github.octaviusframework.types.PgType
import io.github.octaviusframework.types.TypeRegistry
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ReflectionCompositeParameterConverter : ParameterConverter<Any> {
    private fun String.toSnakeCase(): String {
        return replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
    }

    override fun canConvert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Boolean {
        if (!source::class.isData) return false
        
        val type = if (expectedOid != null) {
            typeRegistry.types[expectedOid]
        } else {
            val name = source::class.simpleName ?: return false
            val lower = name.lowercase()
            val snake = name.toSnakeCase()
            typeRegistry.types.values.firstOrNull { it is PgType.Composite && (it.name == lower || it.name == snake) }
        }
        
        return type is PgType.Composite
    }

    override fun convert(source: Any, expectedOid: UInt?, typeRegistry: TypeRegistry): Any? {
        val type = if (expectedOid != null) {
            typeRegistry.types[expectedOid] as PgType.Composite
        } else {
            val name = source::class.simpleName ?: return null
            val lower = name.lowercase()
            val snake = name.toSnakeCase()
            typeRegistry.types.values.first { it is PgType.Composite && (it.name == lower || it.name == snake) } as PgType.Composite
        }

        val properties = source::class.memberProperties.associateBy { it.name.lowercase() }
        
        val fields = type.attributes.map { (attrName, attributeOid) ->
            val prop = properties[attrName.lowercase()]
                ?: properties[attrName.replace("_", "").lowercase()]
            
            var value = if (prop != null) {
                prop.isAccessible = true
                prop.call(source)
            } else null

            if (value != null) {
                value = typeRegistry.parameterConverterRegistry.convert(value, attributeOid, typeRegistry)
            }

            if (value is io.github.octaviusframework.container.PgContainer) {
                ContainerField(rawValue = null, container = value, value = null)
            } else {
                ContainerField(rawValue = null, container = null, value = value)
            }
        }

        return PgComposite(type, fields, typeRegistry)
    }
}
