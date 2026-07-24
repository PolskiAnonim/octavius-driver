package io.github.octaviusframework.spring.exception

import io.github.octaviusframework.driver.exception.SQLExceptionWrapper
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.support.SQLExceptionTranslator
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
import java.sql.SQLException

/**
 * Translates Octavius-specific SQLExceptions into Spring's DataAccessException hierarchy.
 */
class OctaviusExceptionTranslator : SQLExceptionTranslator {

    private val fallbackTranslator = SQLStateSQLExceptionTranslator()

    override fun translate(task: String, sql: String?, ex: SQLException): DataAccessException? {
        if (ex is SQLExceptionWrapper) {
            return OctaviusDataAccessException(ex.wrappedException)
        }

        var cause: Throwable? = ex.cause
        while (cause != null) {
            if (cause is SQLExceptionWrapper) {
                return OctaviusDataAccessException(cause.wrappedException)
            }
            cause = cause.cause
        }

        return fallbackTranslator.translate(task, sql, ex)
    }
}
