package dev.limebeck.templateEngine.inputStream

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
        if (!hasNext() || it != peek()) {
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

fun <T, R> simpleComparator(value: R, next: T): Boolean {
    return value == next
}

fun <T, R> InputStream<T>.skipNext(values: List<R>, comparator: (value: R, next: T) -> Boolean = ::simpleComparator) {
    values.fold(listOf<T>()) { acc, value ->
        if (!hasNext())
            throw StreamError(
                message = "<17e0c3e5> Expected $values but got EOF at position $currentPosition",
                position = currentPosition
            )
        val result = acc + peek()
        if (comparator(value, peek())) {
            next()
        } else {
            throw StreamError(
                message = "<17e0c3e5> Expected $values but got $result at position $currentPosition",
                position = currentPosition
            )
        }
        result
    }
}

data class SimplePosition(
    override val absolutePosition: Int
) : InputStream.Position {
    companion object {
        val MOCK = SimplePosition(-1)
    }

    override fun copy(): InputStream.Position = this.copy(absolutePosition = absolutePosition)
    override fun equals(other: Any?): Boolean {
        return if (
            this.absolutePosition == MOCK.absolutePosition
            || (other as? InputStream.Position)?.absolutePosition == MOCK.absolutePosition
        )
            true
        else
            super.equals(other)
    }
}

fun <T> Collection<T>.toStream(): RewindableInputStream<T> {
    return object : RewindableInputStream<T> {
        private var position: SimplePosition = SimplePosition(0)
        private val collection = this@toStream
        private var nextValue: T? = collection.firstOrNull()
        private val collectionSize by lazy { collection.size }

        override fun peek(): T {
            if (!hasNext()) {
                throw StreamError(
                    position,
                    "<e0920d18> Invalid position. Max allowed absolutePosition - ${collectionSize - 1}"
                )
            }
            return nextValue!!
        }

        override fun next(): T {
            val value = nextValue!!

            seek(position.absolutePosition + 1)
            nextValue = collection.elementAtOrNull(position.absolutePosition)

            return value
        }

        override fun seek(absolutePosition: Int) {
            position = position.copy(absolutePosition = absolutePosition)
            nextValue = collection.elementAtOrNull(position.absolutePosition)
        }

        override fun hasNext(): Boolean = collection.size > position.absolutePosition

        override val currentPosition: InputStream.Position
            get() = position
    }
}

fun <T> recoverable(stream: RewindableInputStream<*>, block: () -> T): T {
    val recoverPosition = stream.currentPosition.absolutePosition
    try {
        return block()
    } finally {
        stream.seek(recoverPosition)
    }
}

fun <T> InputStream<T>.peekOrNull(): T? = try {
    peek()
} catch (e: Throwable) {
    null
}