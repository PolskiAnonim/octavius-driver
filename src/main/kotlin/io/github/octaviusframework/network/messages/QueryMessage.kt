package io.github.octaviusframework.network.messages

import io.github.octaviusframework.io.PgOutputStream

class QueryMessage(private val query: String) : FrontendMessage {
    override fun encode(out: PgOutputStream) {
        val queryBytes = query.toByteArray(Charsets.UTF_8)
        val length = 4 + queryBytes.size + 1 // length itself + bytes + null terminator
        
        out.writeByte('Q'.code.toByte())
        out.writeInt(length)
        out.writeBytes(queryBytes)
        out.writeByte(0)
    }
}
