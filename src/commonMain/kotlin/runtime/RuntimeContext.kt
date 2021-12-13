package dev.limebeck.templateEngine.runtime

interface RuntimeContext {
    fun get(key: String): RuntimeObject
    fun set(key: String, obj: RuntimeObject)
    operator fun plus(another: RuntimeContext): RuntimeContext
}

fun Any?.wrap(): RuntimeObject =
    when (this) {
        null -> RuntimeObject.Null
        is RuntimeObject -> this
        is kotlin.String -> RuntimeObject.StringWrapper(this)
        is kotlin.Number -> RuntimeObject.NumberWrapper(this)
        is kotlin.Boolean -> RuntimeObject.BooleanWrapper(this)
        is Collection<*> -> RuntimeObject.CollectionWrapper(this.mapNotNull { it?.wrap() })
        is Map<*, *> -> RuntimeObject.ObjectWrapper(this.entries.associate {
            val key = it.key
            val value = it.value
            if (key !is String)
                throw RuntimeException("<22571494> Unsupported key '$key' in object $this")
            key to value.wrap()
        })

        else -> throw RuntimeException("<13f5b28> Unsupported context item $this")
    }

fun Map<String, Any>.wrapAll(): Map<String, RuntimeObject> =
    map {
        val value = it.value
        it.key to it.value.wrap()
    }.toMap().toMutableMap()

sealed interface RuntimeObject {
    class CallableWrapper(
        val block: (args: List<RuntimeObject>) -> RuntimeObject
    ) : RuntimeObject {
        companion object {
            fun from(block: (args: List<RuntimeObject>) -> Any): CallableWrapper {
                return CallableWrapper { args: List<RuntimeObject> ->
                    block(args).wrap()
                }
            }
        }
    }

    object Null : RuntimeObject

    class ObjectWrapper(
        val obj: Map<String, RuntimeObject>
    ) : RuntimeObject

    class CollectionWrapper(
        val collection: List<RuntimeObject>
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