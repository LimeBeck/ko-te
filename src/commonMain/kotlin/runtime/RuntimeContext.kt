package dev.limebeck.templateEngine.runtime

interface RuntimeContext {
    fun get(key: String): RuntimeObject
    fun set(key: String, obj: RuntimeObject)
}

sealed interface RuntimeObject {
    class Callable(
        val block: (args: List<Any>) -> Any
    ) : RuntimeObject

    class Value(
        val value: Any
    ) : RuntimeObject
}

data class RuntimeException(
    val reason: String? = null,
    val underlyingException: Throwable? = null
) : Throwable(reason, underlyingException)

data class MapContext(
    private val context: MutableMap<String, Any>
) : RuntimeContext {
    companion object {
        val EMPTY = MapContext(mutableMapOf())
    }

    override fun get(key: String): RuntimeObject {
        return context[key] as? RuntimeObject ?: throw RuntimeException("<52662afc> Context not found by key '$key'")
    }

    override fun set(key: String, obj: RuntimeObject) {
        context[key] = obj
    }
}

fun Map<String, RuntimeObject>.asContext() = MapContext(this.toMutableMap())