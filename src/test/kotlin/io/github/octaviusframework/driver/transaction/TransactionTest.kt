package io.github.octaviusframework.driver.transaction

import io.github.octaviusframework.driver.jdbc.OctaviusConnection
import io.github.octaviusframework.driver.jdbc.getOctaviusConnection
import io.github.octaviusframework.driver.query.get
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.SQLException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TransactionTest {

    private lateinit var connection: OctaviusConnection

    @BeforeEach
    fun setup() {
        val props = Properties()
        props.setProperty("user", "postgres")
        props.setProperty("password", "1234")

        connection = getOctaviusConnection("jdbc:octavius://localhost:5432/octavius_test", props)

        connection.createNativeQuery("CREATE TEMP TABLE IF NOT EXISTS test_trx (id INT, value TEXT)").execute()
        connection.createNativeQuery("TRUNCATE TABLE test_trx").execute()
    }

    @AfterEach
    fun teardown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    private fun countRows(): Long {
        val rows = connection.createNativeQuery("SELECT COUNT(*) FROM test_trx").fetchAll()
        return rows[0].get<Long>(0)
    }

    @Test
    fun `test autoCommit false requires explicit commit`() {
        connection.autoCommit = false
        assertFalse(connection.autoCommit)

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()
        assertEquals(1L, countRows())

        connection.commit() // This should send COMMIT

        // Verify data is still there outside transaction
        assertEquals(1L, countRows())
    }

    @Test
    fun `test rollback`() {
        connection.autoCommit = false

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()
        assertEquals(1L, countRows())

        connection.rollback()

        // Verify data is not there
        assertEquals(0L, countRows())
    }

    @Test
    fun `test savepoints`() {
        connection.autoCommit = false

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()

        val sp1 = connection.setSavepoint("sp1")

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (2, 'B')").execute()

        assertEquals(2L, countRows())

        connection.rollback(sp1)

        assertEquals(1L, countRows())

        connection.commit()

        assertEquals(1L, countRows())
    }

    @Test
    fun `test release savepoint`() {
        connection.autoCommit = false

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()

        val sp1 = connection.setSavepoint()

        connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (2, 'B')").execute()

        connection.releaseSavepoint(sp1)

        connection.commit()

        assertEquals(2L, countRows())
    }

    @Test
    fun `test transaction state`() {
        assertEquals(OctaviusConnection.TransactionState.IDLE, connection.transactionState)

        connection.autoCommit = false

        assertEquals(OctaviusConnection.TransactionState.IN_TRANSACTION, connection.transactionState)

        try {
            connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES ('INVALID_INT', 'A')").execute()
        } catch (e: SQLException) {
            // Expected syntax error
        }

        assertEquals(OctaviusConnection.TransactionState.FAILED, connection.transactionState)

        connection.rollback()
        assertEquals(OctaviusConnection.TransactionState.IN_TRANSACTION, connection.transactionState)
    }

    @Test
    fun `test transaction manager successful block`() {
        connection.transaction {
            connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()
        }

        // Verify data was committed
        assertEquals(1L, countRows())
        // Verify autoCommit was restored to true
        assertEquals(true, connection.autoCommit)
    }

    @Test
    fun `test transaction manager failing block rolls back`() {
        try {
            connection.transaction {
                connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()
                throw RuntimeException("Simulated error")
            }
        } catch (e: RuntimeException) {
            assertEquals("Simulated error", e.message)
        }

        // Verify data was rolled back
        assertEquals(0L, countRows())
        // Verify autoCommit was restored to true
        assertEquals(true, connection.autoCommit)
    }

    @Test
    fun `test transaction manager savepoint successful block`() {
        connection.transaction {
            connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()

            withSavepoint("sp1") {
                connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (2, 'B')").execute()
            }
        }

        // Verify both were committed
        assertEquals(2L, countRows())
    }

    @Test
    fun `test transaction manager savepoint failing block rolls back to savepoint`() {
        connection.transaction {
            connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (1, 'A')").execute()

            try {
                withSavepoint {
                    connection.createNativeQuery("INSERT INTO test_trx (id, value) VALUES (2, 'B')").execute()
                    throw RuntimeException("Simulated error in savepoint")
                }
            } catch (e: RuntimeException) {
                assertEquals("Simulated error in savepoint", e.message)
            }
            
            // Should still be 1 row within transaction after savepoint rollback
            assertEquals(1L, countRows())
        }

        // Verify only the first was committed
        assertEquals(1L, countRows())
    }
}
