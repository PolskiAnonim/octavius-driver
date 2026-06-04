package io.github.octaviusframework

import java.sql.DriverManager
import java.util.Properties
import io.github.octaviusframework.jdbc.OctaviusConnection
import io.github.octaviusframework.query.get
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull

class OctaviusTest {

    @Test
    fun test() {
        println("Zaczynamy test!")
        
        val props = Properties()
        props.setProperty("user", "postgres")
        props.setProperty("password", "1234")
        
        val connection = DriverManager.getConnection("jdbc:octavius://localhost:5432/postgres", props)
        val octaviusConn1 = connection.unwrap(OctaviusConnection::class.java)
        
        println("Tworzę testowy kompozyt w bazie...")
        octaviusConn1.queryExecutor.execute("DROP TYPE IF EXISTS my_custom_composite CASCADE")
        octaviusConn1.queryExecutor.execute("CREATE TYPE my_custom_composite AS (id int, name text)")
        
        // Nawiązujemy nowe połączenie, aby TypeRegistry załadowało nowo utworzony typ z bazy
        val connection2 = DriverManager.getConnection("jdbc:octavius://localhost:5432/postgres", props)
        val octaviusConn2 = connection2.unwrap(OctaviusConnection::class.java)
        
        println("Sukces, testujemy pobieranie kompozytu z bazy...")
        val result = octaviusConn2.queryExecutor.query("SELECT ROW(42, 'Hello Octavius!')::my_custom_composite AS my_comp")
        
        val fieldMeta = result.firstOrNull()?.fields?.get(0)?.descriptor
        val compositeOid = fieldMeta?.dataTypeOid ?: throw IllegalStateException("Brak opisu kolumny")

        println("Pobieramy kolumnę 'my_comp' o OID typu: $compositeOid")

        val result2 = octaviusConn2.queryExecutor.query("SELECT '123'::text AS txt")
        val txt = result2.first().get<String>("txt")
        println(txt)

        assertNotNull(txt)
    }
}
