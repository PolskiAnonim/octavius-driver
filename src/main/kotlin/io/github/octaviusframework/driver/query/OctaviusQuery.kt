package io.github.octaviusframework.driver.query

import io.github.octaviusframework.driver.mapping.parameter.ParameterConverter
import io.github.octaviusframework.driver.mapping.parameter.ParameterConverterRegistry
import io.github.octaviusframework.driver.mapping.result.ResultConverter
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
    val resultConverterRegistry = ResultConverterRegistry(parent = typeRegistry.converterRegistry)
    val parameterConverterRegistry = ParameterConverterRegistry(parent = typeRegistry.parameterConverterRegistry)
    private val localDeserializer = ResultMapper(resultConverterRegistry)

    private val parameters = mutableListOf<Any?>()
    private val parameterSerializer = ParameterSerializer(typeRegistry, parameterConverterRegistry)

    fun registerResultConverter(converter: ResultConverter<*>): OctaviusQuery {
        resultConverterRegistry.addConverter(converter)
        return this
    }

    fun registerParameterConverter(converter: ParameterConverter<*>): OctaviusQuery {
        parameterConverterRegistry.addConverter(converter)
        return this
    }

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
