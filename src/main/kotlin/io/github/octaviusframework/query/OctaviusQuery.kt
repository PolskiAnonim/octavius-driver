package io.github.octaviusframework.query

import io.github.octaviusframework.types.TypeRegistry

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
        return queryExecutor.query(sql, types, values)
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
