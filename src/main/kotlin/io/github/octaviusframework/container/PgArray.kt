package io.github.octaviusframework.container

import io.github.octaviusframework.io.ByteArrayWindow
import io.github.octaviusframework.types.TypeRegistry
import io.github.octaviusframework.exceptions.OctaviusTypeException
import io.github.octaviusframework.exceptions.TypeExceptionMessage
import io.github.octaviusframework.types.PgType
import io.github.octaviusframework.types.TypeSerializer

/**
 * Reprezentuje pojedynczy wymiar tablicy w Postgresie.
 */
data class ArrayDimension(
    val size: Int,
    val lowerBound: Int
)


class PgArray internal constructor(
    val arrayOid: UInt,
    val elementOid: UInt,
    val dimensions: List<ArrayDimension>,
    var windows: MutableList<ByteArrayWindow?>?,
    var containers: MutableList<PgContainer?>?,
    var values: MutableList<Any?>?,
    @PublishedApi internal val typeRegistry: TypeRegistry
) : PgContainer {

    val totalElements: Int
        get() = windows?.size ?: containers?.size ?: values?.size ?: 0

    operator fun set(index: Int, newValue: Any?) {
        if (newValue is PgContainer) {
            if (containers == null) {
                val elementType = typeRegistry.types[elementOid]
                if (elementType !is PgType.Composite &&
                    elementType !is PgType.Array &&
                    elementType !is PgType.Range &&
                    elementType !is PgType.Multirange) {
                    throw OctaviusTypeException(
                        TypeExceptionMessage.NOT_A_CONTAINER,
                        oid = elementOid,
                        details = "Tablica typu OID $elementOid nie przechowuje kontenerów"
                    )
                }
                containers = MutableList(totalElements) { null }
            }
            containers!![index] = newValue
            windows?.set(index, null)
            values?.let { it[index] = null }
        } else {
            if (values == null) {
                values = MutableList(totalElements) { null }
            }
            values!![index] = newValue
            windows?.set(index, null)
            containers?.set(index, null)
        }
    }

    fun setElement(indices: IntArray, newValue: Any?) {
        require(indices.size == dimensions.size) { "Oczekiwano ${dimensions.size} indeksów dla tablicy wielowymiarowej, otrzymano ${indices.size}" }
        val flatIndex = indices.foldIndexed(0) { idx, acc, i ->
            acc * dimensions[idx].size + i
        }
        set(flatIndex, newValue)
    }

    fun setDimension(indices: IntArray, newValues: Iterable<Any?>) {
        val expectedIndices = dimensions.size - 1
        require(indices.size == expectedIndices) { "Oczekiwano $expectedIndices indeksów do określenia wymiaru, otrzymano ${indices.size}" }
        val lastDimensionSize = dimensions.last().size
        
        var baseIndex = 0
        if (expectedIndices > 0) {
            for (i in indices.indices) {
                var multiplier = 1
                for (j in i + 1 until dimensions.size) {
                    multiplier *= dimensions[j].size
                }
                baseIndex += indices[i] * multiplier
            }
        }
        
        val list = newValues.toList()
        require(list.size == lastDimensionSize) { "Oczekiwano $lastDimensionSize wartości dla ostatniego wymiaru, otrzymano ${list.size}" }
        
        list.forEachIndexed { i, value ->
            set(baseIndex + i, value)
        }
    }

    fun setDimension(indices: IntArray, vararg newValues: Any?) {
        setDimension(indices, newValues.toList())
    }

    fun setAll(newValues: Iterable<Any?>) {
        val list = newValues.toList()
        require(list.size == totalElements) { "Oczekiwano $totalElements elementów, otrzymano ${list.size}" }
        list.forEachIndexed { i, value ->
            set(i, value)
        }
    }

    fun setAll(vararg newValues: Any?) {
        setAll(newValues.toList())
    }

    inline fun <reified T> getElement(indices: IntArray): T? {
        require(indices.size == dimensions.size) { "Oczekiwano ${dimensions.size} indeksów dla tablicy wielowymiarowej, otrzymano ${indices.size}" }
        val flatIndex = indices.foldIndexed(0) { idx, acc, i ->
            acc * dimensions[idx].size + i
        }
        return get<T>(flatIndex)
    }

    @PublishedApi
    internal val elementSerializer: TypeSerializer<Any>? by lazy {
        typeRegistry.getSerializerByOid(elementOid)
    }

    inline fun <reified T> get(index: Int): T? {
        if (values != null && values!![index] != null) return values!![index] as T
        if (containers != null) return containers!![index] as? T

        val window = windows!![index] ?: return null
        val serializer = elementSerializer
            ?: throw OctaviusTypeException(
                TypeExceptionMessage.MISSING_SERIALIZER,
                oid = elementOid,
                details = "Pobieranie elementu tablicy"
            )

        val parsedValue = serializer.fromBinary(window)
        if (parsedValue is T) return parsedValue
        throw OctaviusTypeException(
            TypeExceptionMessage.CASTING_ERROR,
            typeName = T::class.simpleName,
            details = "Otrzymano ${parsedValue::class.simpleName}"
        )
    }

    override fun detach() {
        windows?.forEach { it?.detach() }
        containers?.forEach { it?.detach() }
    }

    /**
     * Konwertuje całą tablicę do płaskiej Listy obiektów docelowego typu.
     * Tutaj następuje faktyczne (leniwe) parsowanie z ByteArray na właściwe typy.
     */
    inline fun <reified T> toList(): List<T?> {
        val count = totalElements
        val result = ArrayList<T?>(count)

        val serializer = if (containers == null) elementSerializer else null
        if (containers == null && serializer == null) {
            throw OctaviusTypeException(
                TypeExceptionMessage.MISSING_SERIALIZER,
                oid = elementOid,
                details = "Element tablicy"
            )
        }

        for (i in 0 until count) {
            if (values != null && values!![i] != null) {
                result.add(values!![i] as T)
                continue
            }
            if (containers != null) {
                val element = containers!![i]
                if (element == null) result.add(null)
                else result.add(element as T)
                continue
            }
            val window = windows!![i]
            if (window == null) {
                result.add(null)
                continue
            }
            val parsedValue = serializer!!.fromBinary(window)
            if (parsedValue is T) {
                result.add(parsedValue)
            } else {
                throw OctaviusTypeException(
                    TypeExceptionMessage.CASTING_ERROR,
                    typeName = T::class.simpleName,
                    details = "Otrzymano ${parsedValue::class.simpleName}"
                )
            }
        }
        return result
    }
}
