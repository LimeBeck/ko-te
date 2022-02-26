package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.Resource

interface RuntimeContext {
    val resources: List<Resource>
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
        val block: (args: List<RuntimeObject>, context: RuntimeContext) -> RuntimeObject
    ) : RuntimeObject {
        companion object {
            fun from(block: (args: List<RuntimeObject>, context: RuntimeContext) -> Any): CallableWrapper {
                return CallableWrapper { args: List<RuntimeObject>, context: RuntimeContext ->
                    block(args, context).wrap()
                }
            }
        }
    }

    object Null : RuntimeObject
    object Nothing : RuntimeObject

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

class MapContext(
    predefinedRuntimeObjects: Map<String, RuntimeObject>,
    override val resources: List<Resource> = emptyList()
) : RuntimeContext {
    companion object {
        val EMPTY = MapContext(mutableMapOf())
    }

    private val runtimeObjects = predefinedRuntimeObjects.toMutableMap()

    override fun get(key: String): RuntimeObject {
        return runtimeObjects[key] ?: throw RuntimeException("<52662afc> Context not found by key '$key'")
    }

    override fun set(key: String, obj: RuntimeObject) {
        runtimeObjects[key] = obj
    }

    override fun plus(another: RuntimeContext): RuntimeContext {
        return if (another is MapContext)
            MapContext(
                (this.runtimeObjects + another.runtimeObjects).toMutableMap(),
                this.resources + another.resources
            )
        else throw RuntimeException("<c99fdcd8> Can`t merge contexts $this and $another")
    }

    operator fun plus(another: Map<String, RuntimeObject>): RuntimeContext {
        return MapContext(
            (this.runtimeObjects + another).toMutableMap(),
            this.resources
        )
    }
}

fun Map<String, RuntimeObject>.asContext() =
    MapContext(this.toMutableMap(), emptyList())