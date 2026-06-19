package io.github.octaviusframework.driver.codec

import io.github.octaviusframework.driver.io.ByteArrayWindow

/**
 * Zoptymalizowany bufor do budowania pakietów binarnych dla bazy danych.
 * Pozwala na rezerwowanie miejsca na rozmiar i późniejsze jego wypełnienie bez kopiowania pamięci.
 */
class PgByteWriter(initialCapacity: Int = 1024) {
    var data = ByteArray(initialCapacity)
        private set
    var position = 0
        private set

    private fun ensureCapacity(needed: Int) {
        if (position + needed > data.size) {
            var newCap = data.size * 2
            while (position + needed > newCap) newCap *= 2
            data = data.copyOf(newCap)
        }
    }

    fun writeByte(b: Byte) {
        ensureCapacity(1)
        data[position++] = b
    }

    fun writeInt(i: Int) {
        ensureCapacity(4)
        data[position++] = (i shr 24).toByte()
        data[position++] = (i shr 16).toByte()
        data[position++] = (i shr 8).toByte()
        data[position++] = i.toByte()
    }

    fun writeUInt(u: UInt) {
        writeInt(u.toInt())
    }

    fun writeShort(s: Short) {
        ensureCapacity(2)
        val intValue = s.toInt()
        data[position++] = (intValue shr 8).toByte()
        data[position++] = intValue.toByte()
    }

    fun writeBytes(bytes: ByteArray) {
        ensureCapacity(bytes.size)
        bytes.copyInto(data, position)
        position += bytes.size
    }

    fun writeBytes(window: ByteArrayWindow) {
        ensureCapacity(window.length)
        window.data.copyInto(data, position, window.offset, window.offset + window.length)
        position += window.length
    }

    /**
     * Zostawia 4 bajty miejsca na liczbę (np. rozmiar paczki) i zwraca indeks,
     * pod którym ten rozmiar ma zostać później zapisany.
     */
    fun reserveLengthInt(): Int {
        val marker = position
        writeInt(0) // placeholder
        return marker
    }

    /**
     * Oblicza ilość bajtów dodanych od momentu wywołania reserveLengthInt i wpisuje tę wartość pod marker.
     * Nie wlicza 4 bajtów samego markera (zgodnie z wieloma strukturami PG).
     */
    fun fillLengthInt(markerIndex: Int) {
        val length = position - markerIndex - 4
        data[markerIndex] = (length shr 24).toByte()
        data[markerIndex + 1] = (length shr 16).toByte()
        data[markerIndex + 2] = (length shr 8).toByte()
        data[markerIndex + 3] = length.toByte()
    }

    fun toByteArray(): ByteArray {
        return data.copyOfRange(0, position)
    }
}