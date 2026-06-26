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
    val typeRegistry: TypeRegistry
) {
    val resultConverterRegistry = ResultConverterRegistry(parent = typeRegistry.converterRegistry)
    val parameterConverterRegistry = ParameterConverterRegistry(parent = typeRegistry.parameterConverterRegistry)
    private val localDeserializer = ResultMapper(resultConverterRegistry)
    private val parameterSerializer = ParameterSerializer(typeRegistry, parameterConverterRegistry)

    fun registerResultConverter(converter: ResultConverter<*>): OctaviusQuery {
        resultConverterRegistry.addConverter(converter)
        return this
    }

    fun registerParameterConverter(converter: ParameterConverter<*>): OctaviusQuery {
        parameterConverterRegistry.addConverter(converter)
        return this
    }

    private fun serializeParameters(params: List<Any?>): Pair<List<UInt>, List<ByteArray?>> {
        return parameterSerializer.serializeAll(params)
    }

    // --- no arguments ---
    fun fetchAll(): List<Row> = queryExecutor.query(sql, emptyList(), emptyList(), localDeserializer)
    fun fetchOne(): Row? = fetchAll().firstOrNull()
    fun executeUpdate(): Long = queryExecutor.update(sql, emptyList(), emptyList())
    fun execute() = queryExecutor.execute(sql)

    // --- positional arguments ---

    fun fetchAll(param: Any?, vararg params: Any?): List<Row> {
        val (types, values) = serializeParameters(listOf(param) + params.toList())
        return queryExecutor.query(sql, types, values, localDeserializer)
    }

    fun fetchOne(param: Any?, vararg params: Any?): Row? {
        val rows = fetchAll(param, *params)
        return rows.firstOrNull()
    }

    fun executeUpdate(param: Any?, vararg params: Any?): Long {
        val (types, values) = serializeParameters(listOf(param) + params.toList())
        return queryExecutor.update(sql, types, values)
    }

    fun execute(param: Any?, vararg params: Any?) {
        val (types, values) = serializeParameters(listOf(param) + params.toList())
        queryExecutor.update(sql, types, values)
    }

    // --- named arguments (Map) ---

    private fun prepareNamedQuery(params: Map<String, Any?>): Triple<String, List<UInt>, List<ByteArray?>> {
        val parsed = SqlParameterParser.parse(sql)
        val listParams = parsed.paramNames.map { 
            if (!params.containsKey(it)) {
                throw IllegalArgumentException("Missing parameter: $it")
            }
            params[it]
        }
        val (types, values) = serializeParameters(listParams)
        return Triple(parsed.transformedSql, types, values)
    }

    @JvmName("fetchAllNamedMap")
    fun fetchAll(params: Map<String, Any?>): List<Row> {
        val (transformedSql, types, values) = prepareNamedQuery(params)
        return queryExecutor.query(transformedSql, types, values, localDeserializer)
    }

    @JvmName("fetchOneNamedMap")
    fun fetchOne(params: Map<String, Any?>): Row? {
        val rows = fetchAll(params)
        return rows.firstOrNull()
    }

    @JvmName("executeUpdateNamedMap")
    fun executeUpdate(params: Map<String, Any?>): Long {
        val (transformedSql, types, values) = prepareNamedQuery(params)
        return queryExecutor.update(transformedSql, types, values)
    }

    @JvmName("executeNamedMap")
    fun execute(params: Map<String, Any?>) {
        val (transformedSql, types, values) = prepareNamedQuery(params)
        queryExecutor.update(transformedSql, types, values)
    }

    // --- named arguments (Pairs) ---

    @JvmName("fetchAllNamedPairs")
    fun fetchAll(param: Pair<String, Any?>, vararg params: Pair<String, Any?>): List<Row> = fetchAll(mapOf(param) + params.toMap())

    @JvmName("fetchOneNamedPairs")
    fun fetchOne(param: Pair<String, Any?>, vararg params: Pair<String, Any?>): Row? = fetchOne(mapOf(param) + params.toMap())

    @JvmName("executeUpdateNamedPairs")
    fun executeUpdate(param: Pair<String, Any?>, vararg params: Pair<String, Any?>): Long = executeUpdate(mapOf(param) + params.toMap())

    @JvmName("executeNamedPairs")
    fun execute(param: Pair<String, Any?>, vararg params: Pair<String, Any?>) = execute(mapOf(param) + params.toMap())
}
