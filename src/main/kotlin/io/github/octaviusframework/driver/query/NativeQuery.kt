package io.github.octaviusframework.driver.query

import io.github.octaviusframework.driver.type.TypeRegistry

class NativeQuery(
    sql: String,
    queryExecutor: QueryExecutor,
    typeRegistry: TypeRegistry
) : OctaviusQuery<NativeQuery>(sql, queryExecutor, typeRegistry) {

    fun fetchAll(vararg params: Any?): List<Row> {
        val (types, values) = serializeParameters(params.toList())
        return queryExecutor.query(sql, types, values, localDeserializer)
    }

    fun fetchOne(vararg params: Any?): Row {
        val rows = fetchAll(*params)
        check(rows.size == 1) { "Expected exactly one row, but got ${rows.size}" }
        return rows.first()
    }

    fun fetchOneOrNull(vararg params: Any?): Row? {
        val rows = fetchAll(*params)
        check(rows.size <= 1) { "Expected 0 or 1 row, but got ${rows.size}" }
        return rows.firstOrNull()
    }

    fun update(vararg params: Any?): Long {
        val (types, values) = serializeParameters(params.toList())
        return queryExecutor.update(sql, types, values)
    }

    fun execute() {
        queryExecutor.execute(sql)
    }
}
