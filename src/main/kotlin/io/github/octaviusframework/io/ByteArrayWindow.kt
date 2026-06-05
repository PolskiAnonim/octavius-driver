package io.github.octaviusframework.io

/**
 * Reprezentuje wycinek głównego bufora wiersza.
 * Zero kopiowania bajtów!
 */
class ByteArrayWindow(
    var data: ByteArray,
    var offset: Int,
    val length: Int
) {
    /**
     * Tworzy nowe "pod-okno" dla zagnieżdżonych struktur (np. tablicy w kompozycie).
     */
    fun slice(relativeOffset: Int, sliceLength: Int): ByteArrayWindow {
        require(relativeOffset + sliceLength <= length) { "Slice out of bounds" }
        return ByteArrayWindow(data, this.offset + relativeOffset, sliceLength)
    }

    /**
     * Kopiuje wycinek do nowej, odseparowanej tablicy, uwalniając referencję do całego bufora.
     */
    fun detach() {
        if (offset == 0 && data.size == length) return
        val newData = data.copyOfRange(offset, offset + length)
        this.data = newData
        this.offset = 0
    }
}
