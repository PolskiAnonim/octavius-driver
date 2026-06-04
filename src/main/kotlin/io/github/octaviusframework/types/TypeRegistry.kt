package io.github.octaviusframework.types

import kotlin.reflect.KClass

data class PgType(
    val oid: Int,
    val name: String,
    val relationId: Int, // OID tabeli (jeśli to kompozyt)
    val elementId: Int, // OID elementu (jeśli to tablica)
    val arrayId: Int // OID typu tablicowego dla tego typu
)

data class PgAttribute(
    val relationId: Int,
    val attnum: Int,
    val name: String,
    val typeOid: Int
)

class TypeRegistry {
    val types = mutableMapOf<Int, PgType>()
    val relationAttributes = mutableMapOf<Int, MutableList<PgAttribute>>()
    
    private val handlersByOid = mutableMapOf<Int, TypeHandler<*>>()
    private val handlersByClass = mutableMapOf<KClass<*>, TypeHandler<*>>()
    private val handlersByName = mutableMapOf<String, TypeHandler<*>>()

    init {
        // Rejestrujemy wbudowane typy
        registerHandler(ShortHandler)
        registerHandler(IntHandler)
        registerHandler(LongHandler)
        registerHandler(FloatHandler)
        registerHandler(DoubleHandler)
        registerHandler(BooleanHandler)
        registerHandler(StringHandler)
        registerHandler(ByteArrayHandler)
    }

    fun registerHandler(handler: TypeHandler<*>) {
        val fullName = "${handler.pgSchema}.${handler.pgTypeName}"
        handlersByName[fullName] = handler
        if (handler.isDefaultForKotlinType) {
            handlersByClass[handler.kotlinClass] = handler
        }
    }

    fun bindOidToHandler(oid: Int, schema: String, name: String) {
        val fullName = "$schema.$name"
        handlersByName[fullName]?.let { handler ->
            handlersByOid[oid] = handler
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getHandlerByOid(oid: Int): TypeHandler<T>? {
        return handlersByOid[oid] as TypeHandler<T>?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getHandlerByClass(kClass: KClass<T>): TypeHandler<T>? {
        return handlersByClass[kClass] as TypeHandler<T>?
    }

    fun clearOidMappings() {
        types.clear()
        relationAttributes.clear()
        handlersByOid.clear()
    }
}
