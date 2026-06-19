package io.github.octaviusframework.driver.io

fun ByteArrayWindow.getShortBE(relativeOffset: Int = 0): Short {
    return this.data.getShortBE(this.offset + relativeOffset)
}

fun ByteArrayWindow.getIntBE(relativeOffset: Int = 0): Int {
    return this.data.getIntBE(this.offset + relativeOffset)
}

fun ByteArrayWindow.getLongBE(relativeOffset: Int = 0): Long {
    return this.data.getLongBE(this.offset + relativeOffset)
}

fun ByteArrayWindow.getFloatBE(relativeOffset: Int = 0): Float {
    return Float.fromBits(this.getIntBE(relativeOffset))
}

fun ByteArrayWindow.getDoubleBE(relativeOffset: Int = 0): Double {
    return Double.fromBits(this.getLongBE(relativeOffset))
}

fun ByteArrayWindow.getUIntBE(relativeOffset: Int = 0): UInt {
    return this.getIntBE(relativeOffset).toUInt()
}

fun ByteArrayWindow.toByteArray(): ByteArray {
    return this.data.copyOfRange(this.offset, this.offset + this.length)
}

operator fun ByteArrayWindow.get(index: Int): Byte {
    require(index in 0 until length) { "Index out of bounds" }
    return this.data[this.offset + index]
}
