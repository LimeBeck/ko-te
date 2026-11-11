package dev.limebeck.templateEngine.inputStream

import dev.limebeck.templateEngine.SourceFile

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
        val sourceFile: SourceFile?
            get() = null
        fun copy(): Position
    }
}

internal fun InputStream.Position.withFile(file: SourceFile?): InputStream.Position = when {
    file == null || sourceFile != null -> this
    this is CharInputStream.StringPosition -> copy(sourceFile = file)
    else -> FilePosition(this, file)
}

internal fun InputStream.Position.formatDiagnostic(message: String): String {
    val file = sourceFile ?: return message
    val location = if (this is CharInputStream.StringPosition) "$line:${column + 1}" else "offset $absolutePosition"
    return "${file.path}:$location: $message"
}

private data class FilePosition(
    private val position: InputStream.Position,
    override val sourceFile: SourceFile
) : InputStream.Position by position {
    override fun copy(): InputStream.Position = copy(position = position.copy())
}

interface RewindableInputStream<T> : InputStream<T> {

    /**
     * Set position in stream
     */
    fun seek(absolutePosition: Int)
}
