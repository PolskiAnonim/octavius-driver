package io.github.octaviusframework.driver.exception

import java.sql.SQLException

internal class SQLExceptionWrapper(val wrappedException: OctaviusException) : SQLException(wrappedException.message, wrappedException.sqlState)