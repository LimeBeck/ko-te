package dev.limebeck.templateEngine.runtime

interface RuntimeContext {
    fun get(key: String): RuntimeObject
    fun set(key: String, obj: RuntimeObject)
    operator fun plus(another: RuntimeContext): RuntimeContext
}

sealed interface RuntimeObject {
    class CallableWrapper(
        val block: (args: List<Any>) -> Any
    ) : RuntimeObject

    class Value(
        val value: Any
    ) : RuntimeObject

    class StringWrapper(
        val string: kotlin.String
    ) : RuntimeObject

    class NumberWrapper(
        val number: kotlin.Number
    ) : RuntimeObject

    class BooleanWrapper(
        val value: kotlin.Boolean
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

    override fun plus(another: RuntimeContext): RuntimeContext {
        return if (another is MapContext)
            MapContext((this.context + another.context).toMutableMap())
        else throw RuntimeException("<c99fdcd8> Can`t merge contexts $this and $another")
    }
}

fun Map<String, RuntimeObject>.asContext() = MapContext(this.toMutableMap())