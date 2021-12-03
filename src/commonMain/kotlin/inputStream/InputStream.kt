package dev.limebeck.templateEngine.inputStream

interface InputStream<T> : Iterator<T> {
    /**
     * Peek next item in stream
     * and not change position
     */
    fun peek(): T

    /**
     * Get next item from stream
     * and change position
     */
    override fun next(): T

    /**
     * End of stream marker
     */
    override fun hasNext(): Boolean
    val currentPosition: Position

    interface Position {
        val absolutePosition: Int
        fun copy(): Position
    }
}

interface RewindableInputStream<T> : InputStream<T> {

    /**
     * Set position in stream
     */
    fun seek(absolutePosition: Int)
}