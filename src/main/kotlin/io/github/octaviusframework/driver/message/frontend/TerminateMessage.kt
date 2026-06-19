package io.github.octaviusframework.driver.message.frontend

import io.github.octaviusframework.driver.io.PgOutputStream

/**
 * Wiadomość zakończenia połączenia (Tag 'X').
 */
class TerminateMessage : FrontendMessage {
    override fun encode(out: PgOutputStream) {
        out.writeByte('X'.code.toByte())
        out.writeInt(4)
    }
}
