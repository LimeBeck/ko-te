package dev.limebeck.templateEngine.inputStream

data class StreamError(
    val position: InputStream.Position,
    override val message: String,
    override val cause: Throwable? = null
) : Error(message, cause)