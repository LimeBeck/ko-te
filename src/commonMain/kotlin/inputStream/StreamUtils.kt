package dev.limebeck.templateEngine.inputStream

import dev.limebeck.templateEngine.parser.LexerError


fun <T> InputStream<T>.readUntil(conditionBlock: (value: T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    while (hasNext() && conditionBlock(peek())) {
        result.add(next())
    }
    return result
}

fun <T> InputStream<T>.readSequence(length: Int): List<T> {
    val result = mutableListOf<T>()
    repeat(length) {
        if (hasNext())
            result.add(next())
    }
    return result
}

fun <T> RewindableInputStream<T>.isNextSequenceEquals(value: List<T>): Boolean {
    val lastPosition = currentPosition.absolutePosition

    fun reset() {
        if (currentPosition.absolutePosition != lastPosition) {
            seek(lastPosition)
        }
    }

    value.forEach {
        if (it != peek()) {
            reset()
            return false
        } else
            next()
    }

    reset()

    return true
}

fun <T> InputStream<T>.skipNext(n: Int) {
    repeat(n) {
        if (hasNext())
            next()
    }
}

fun isEmpty(char: Char) = char in listOf(' ', '\n')

fun InputStream<Char>.skipEmpty() {
    while (hasNext() && isEmpty(peek())) {
        next()
    }
}

fun <T> InputStream<T>.skipNext(values: List<T>) {
    values.fold(listOf<T>()) { acc, value ->
        val result = acc + peek()
        if (hasNext()) {
            if (value == peek()) {
                next()
            } else {
                throw StreamError(
                    message = "<17e0c3e5> Expected $values but got $result at position $currentPosition",
                    position = currentPosition
                )
            }
        }
        result
    }
}
