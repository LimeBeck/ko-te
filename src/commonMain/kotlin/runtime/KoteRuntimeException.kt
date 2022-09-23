package dev.limebeck.templateEngine.runtime

data class KoteRuntimeException(
    val reason: String? = null,
    val underlyingException: Throwable? = null
) : Throwable(reason, underlyingException)