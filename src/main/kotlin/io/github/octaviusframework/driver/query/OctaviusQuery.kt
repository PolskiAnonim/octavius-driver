package io.github.octaviusframework.driver.query

import io.github.octaviusframework.driver.mapping.result.ResultConverterRegistry
import io.github.octaviusframework.driver.mapping.result.ResultMapper
import io.github.octaviusframework.driver.type.TypeRegistry

/**
 * Interfejs do wykonywania zapytań z parametrami.
 * Oferuje metody typowe dla frameworków ułatwiających pracę z bazą,
 * takie jak fetchOne() czy fetchAll().
 */
class OctaviusQuery(
    private val sql: String,
    private val queryExecutor: QueryExecutor,
    typeRegistry: TypeRegistry
) {
    val converterRegistry = ResultConverterRegistry(parent = typeRegistry.converterRegistry)
    private val localDeserializer = ResultMapper(converterRegistry)

    private val parameters = mutableListOf<Any?>()
    private val parameterSerializer = ParameterSerializer(typeRegistry)

    /**
     * Dodaje parametr do zapytania.
     */
    fun bind(parameter: Any?): OctaviusQuery {
        parameters.add(parameter)
        return this
    }

    /**
     * Dodaje wiele parametrów do zapytania.
     */
    fun bind(vararg params: Any?): OctaviusQuery {
        parameters.addAll(params)
        return this
    }

    /**
     * Wewnętrzna funkcja do serializacji parametrów wykorzystująca ParameterSerializer.
     */
    private fun serializeParameters(): Pair<List<UInt>, List<ByteArray?>> {
        return parameterSerializer.serializeAll(parameters)
    }

    /**
     * Wykonuje zapytanie i zwraca wszystkie wiersze.
     */
    fun fetchAll(): List<Row> {
        val (types, values) = serializeParameters()
        return queryExecutor.query(sql, types, values, localDeserializer)
    }

    /**
     * Wykonuje zapytanie i zwraca pierwszy wiersz, lub null jeśli brak wyników.
     */
    fun fetchOne(): Row? {
        val rows = fetchAll()
        return rows.firstOrNull()
    }

    /**
     * Wykonuje zapytanie modyfikujące dane (INSERT, UPDATE, DELETE).
     * @return liczba zmodyfikowanych wierszy.
     */
    fun executeUpdate(): Long {
        val (types, values) = serializeParameters()
        return queryExecutor.update(sql, types, values)
    }

    /**
     * Wykonuje zapytanie ogólne.
     */
    fun execute() {
        if (parameters.isEmpty()) {
            queryExecutor.execute(sql)
        } else {
            val (types, values) = serializeParameters()
            queryExecutor.update(sql, types, values)
        }
    }
}
