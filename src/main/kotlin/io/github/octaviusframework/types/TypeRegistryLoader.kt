package io.github.octaviusframework.types

import io.github.octaviusframework.query.QueryExecutor
import java.nio.ByteBuffer

object TypeRegistryLoader {

    fun load(typeRegistry: TypeRegistry, queryExecutor: QueryExecutor) {
        // Krok 1: Pobranie podstawowych typów
        val typesSql = """
            SELECT t.oid, t.typname, t.typrelid, t.typelem, t.typarray, n.nspname 
            FROM pg_catalog.pg_type t
            JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace
        """.trimIndent()
        
        val typesResult = queryExecutor.query(typesSql)
        
        for (row in typesResult) {
            if (row.fields.size < 6) continue
            val oidBytes = row.fields[0].rawValue ?: continue
            val nameBytes = row.fields[1].rawValue ?: continue
            val typrelidBytes = row.fields[2].rawValue ?: continue
            val typelemBytes = row.fields[3].rawValue ?: continue
            val typarrayBytes = row.fields[4].rawValue ?: continue
            val nspnameBytes = row.fields[5].rawValue ?: continue
            
            val oid = ByteBuffer.wrap(oidBytes).int
            val name = String(nameBytes, Charsets.UTF_8)
            val typrelid = ByteBuffer.wrap(typrelidBytes).int
            val typelem = ByteBuffer.wrap(typelemBytes).int
            val typarray = ByteBuffer.wrap(typarrayBytes).int
            val schema = String(nspnameBytes, Charsets.UTF_8)
            
            typeRegistry.types[oid] = PgType(oid, name, typrelid, typelem, typarray)
            
            // Mapujemy ten konkretny OID na handler zdefiniowany w rejestrze
            typeRegistry.bindOidToHandler(oid, schema, name)
        }

        // Krok 2: Pobranie struktury kompozytów z pg_attribute
        val attrSql = "SELECT attrelid, attnum, attname, atttypid FROM pg_catalog.pg_attribute WHERE attnum > 0 AND attisdropped = false ORDER BY attrelid, attnum"
        val attrResult = queryExecutor.query(attrSql)

        for (row in attrResult) {
            if (row.fields.size < 4) continue
            val attrelidBytes = row.fields[0].rawValue ?: continue
            val attnumBytes = row.fields[1].rawValue ?: continue
            val attnameBytes = row.fields[2].rawValue ?: continue
            val atttypidBytes = row.fields[3].rawValue ?: continue

            val attrelid = ByteBuffer.wrap(attrelidBytes).int
            val attnum = ByteBuffer.wrap(attnumBytes).short.toInt()
            val attname = String(attnameBytes, Charsets.UTF_8)
            val atttypid = ByteBuffer.wrap(atttypidBytes).int

            val attr = PgAttribute(attrelid, attnum, attname, atttypid)
            typeRegistry.relationAttributes.getOrPut(attrelid) { mutableListOf() }.add(attr)
        }
    }
}
