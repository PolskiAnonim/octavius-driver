package io.github.octaviusframework.query

import io.github.octaviusframework.network.messages.DataRowMessage
import io.github.octaviusframework.network.messages.RowDescriptionMessage

class QueryResult(
    val rowDescription: RowDescriptionMessage?,
    val rows: List<DataRowMessage>,
    val commandTag: String?
)
