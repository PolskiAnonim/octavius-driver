package io.github.octaviusframework.driver.type

import io.github.octaviusframework.driver.converter.result.mapper.ResultMapper
import io.github.octaviusframework.driver.query.QueryExecutor
import io.github.octaviusframework.driver.query.get

object TypeRegistryLoader {

    fun load(typeRegistry: TypeRegistry, queryExecutor: QueryExecutor, searchPath: List<String>) {
        // typtype is b for a base type, c for a composite type (e.g., a table's row type), d for a domain, e for an enum type, p for a pseudo-type, r for a range type, or m for a multirange type.
        val typesSql = """
            SELECT 
                t.oid, t.typname, t.typelem, t.typarray, t.typtype, t.typbasetype, n.nspname,
                e.enumlabel,
                r.rngsubtype,
                a.attname, a.atttypid,
                r.rngmultitypid
            FROM pg_catalog.pg_type t
            JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace
            LEFT JOIN pg_catalog.pg_enum e ON t.oid = e.enumtypid
            LEFT JOIN pg_catalog.pg_range r ON t.oid = r.rngtypid
            LEFT JOIN pg_catalog.pg_attribute a ON t.typrelid = a.attrelid AND a.attnum > 0 AND a.attisdropped = false
            WHERE NOT (t.typtype = 'c' AND n.nspname IN ('pg_catalog', 'information_schema'))
            AND NOT (t.typtype = 'p' AND t.typname NOT IN ('void', 'record'))
            ORDER BY t.oid, e.enumsortorder, a.attnum
        """.trimIndent()

        val resultMapper = ResultMapper(typeRegistry.converterRegistry)
        val result = queryExecutor.query(typesSql, mapper = resultMapper)

        val enumMap = mutableMapOf<UInt, MutableList<String>>()
        val attrMap = mutableMapOf<UInt, LinkedHashMap<String, UInt>>()
        val rangeMap = mutableMapOf<UInt, UInt>()
        val multirangeMap = mutableMapOf<UInt, UInt>()

        class BaseTypeInfo(
            val name: String, val typelem: UInt, val typarray: UInt,
            val typtype: Char, val typbasetype: UInt, val schema: String
        )

        val parsedTypes = mutableMapOf<UInt, BaseTypeInfo>()

        for (row in result) {
            val oid = row.getRaw(0) as UInt

            // We collect the main type information only the first time for a given OID
            if (oid !in parsedTypes) {
                val name = row.getRaw(1) as String
                val typelem = row.getRaw(2) as UInt
                val typarray = row.getRaw(3) as UInt
                val typtypeString = row.getRaw(4) as String
                val typtype = typtypeString.first()
                val typbasetype = row.getRaw(5) as UInt
                val schema = row.getRaw(6) as String

                parsedTypes[oid] = BaseTypeInfo(name, typelem, typarray, typtype, typbasetype, schema)
            }

            val enumLabel = row.getRaw(7) as String?
            if (enumLabel != null) {
                val enumList = enumMap.getOrPut(oid) { mutableListOf() }
                if (!enumList.contains(enumLabel)) {
                    enumList.add(enumLabel)
                }
            }

            // Range
            val rngSubtype = row.getRaw(8) as UInt?
            if (rngSubtype != null) {
                rangeMap[oid] = rngSubtype

                val multirangeOid = row.getRaw(11) as UInt?
                if (multirangeOid != null && multirangeOid != 0u) {
                    multirangeMap[multirangeOid] = oid
                }
            }

            // Composite
            val attName = row.getRaw(9) as String?
            val attTypid = row.getRaw(10) as UInt?

            if (attName != null && attTypid != null) {
                val attrList = attrMap.getOrPut(oid) { LinkedHashMap() }
                if (!attrList.containsKey(attName)) {
                    attrList[attName] = attTypid
                }
            }
        }

        val newTypes = mutableMapOf<UInt, PgType>()

        // Final construction of correct instance objects for each detected type
        for ((oid, info) in parsedTypes) {
            val pgType = try {
                when {
                    info.typtype == 'e' -> PgType.Enum(oid, info.name, info.schema, enumMap[oid] ?: emptyList())
                    info.typtype == 'd' -> PgType.Domain(oid, info.name, info.schema, info.typbasetype)
                    info.typtype == 'r' -> PgType.Range(oid, info.name, info.schema, rangeMap[oid] ?: error("Missing rangeMap for oid $oid"))
                    info.typtype == 'm' -> PgType.Multirange(oid, info.name, info.schema, multirangeMap[oid] ?: error("Missing multirangeMap for oid $oid"))
                    info.typtype == 'c' -> {
                        val attrs = attrMap[oid] ?: LinkedHashMap()
                        PgType.Composite(oid, info.name, info.schema, attrs)
                    }

                info.typtype == 'p' -> {
                    when (info.name) {
                        "record" -> PgType.Record(oid, info.name, info.schema)
                        "void" -> PgType.Void(oid, info.name, info.schema)
                        else -> error("Unreachable code: unexpected pseudo-type ${info.name}")
                    }
                }

                info.typelem != 0u && info.typarray == 0u -> PgType.Array(oid, info.name, info.schema, info.typelem)
                else -> PgType.Base(oid, info.name, info.schema)
            }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to parse type ${info.name} (oid $oid, typtype ${info.typtype})", e)
            }

            newTypes[oid] = pgType
        }

        typeRegistry.updateTypes(newTypes, searchPath)
    }
}
