package io.github.octaviusframework.deserialization

import io.github.octaviusframework.container.PgArray
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class IntArrayConverter : PgConverter<IntArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == IntArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): IntArray {
        source as PgArray
        val result = IntArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do IntArray")
            result[i] = context.convert<Int>(value, typeOf<Int>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Int")
        }
        return result
    }
}

class DoubleArrayConverter : PgConverter<DoubleArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == DoubleArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): DoubleArray {
        source as PgArray
        val result = DoubleArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do DoubleArray")
            result[i] = context.convert<Double>(value, typeOf<Double>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Double")
        }
        return result
    }
}

class FloatArrayConverter : PgConverter<FloatArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == FloatArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): FloatArray {
        source as PgArray
        val result = FloatArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do FloatArray")
            result[i] = context.convert<Float>(value, typeOf<Float>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Float")
        }
        return result
    }
}

class LongArrayConverter : PgConverter<LongArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == LongArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): LongArray {
        source as PgArray
        val result = LongArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do LongArray")
            result[i] = context.convert<Long>(value, typeOf<Long>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Long")
        }
        return result
    }
}

class ShortArrayConverter : PgConverter<ShortArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == ShortArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): ShortArray {
        source as PgArray
        val result = ShortArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do ShortArray")
            result[i] = context.convert<Short>(value, typeOf<Short>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Short")
        }
        return result
    }
}

class ByteArrayConverter : PgConverter<ByteArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == ByteArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): ByteArray {
        source as PgArray
        val result = ByteArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do ByteArray")
            result[i] = context.convert<Byte>(value, typeOf<Byte>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Byte")
        }
        return result
    }
}

class BooleanArrayConverter : PgConverter<BooleanArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == BooleanArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): BooleanArray {
        source as PgArray
        val result = BooleanArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do BooleanArray")
            result[i] = context.convert<Boolean>(value, typeOf<Boolean>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Boolean")
        }
        return result
    }
}

class CharArrayConverter : PgConverter<CharArray> {
    override fun canConvert(source: Any, expectedType: KType): Boolean {
        if (source !is PgArray) return false
        return expectedType.classifier == CharArray::class
    }

    override fun convert(source: Any, expectedType: KType, context: DeserializationContext): CharArray {
        source as PgArray
        val result = CharArray(source.totalElements)
        for (i in 0 until source.totalElements) {
            val value = source.get<Any>(i) ?: throw IllegalArgumentException("Nie można wstawić null do CharArray")
            result[i] = context.convert<Char>(value, typeOf<Char>()) ?: throw IllegalArgumentException("Błąd konwersji elementu na Char")
        }
        return result
    }
}
