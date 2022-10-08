package dev.limebeck.templateEngine.runtime

interface RuntimeContext {
    val resourceLoader: ResourceLoader
    val renderer: Renderer
    fun get(key: String): RuntimeObject
    fun set(key: String, obj: RuntimeObject?)
}

fun Any?.wrap(): RuntimeObject =
    when (this) {
        null -> RuntimeObject.Null
        is RuntimeObject -> this
        is String -> RuntimeObject.StringWrapper(this)
        is Number -> RuntimeObject.NumberWrapper(this)
        is Boolean -> RuntimeObject.BooleanWrapper(this)
        is Collection<*> -> RuntimeObject.CollectionWrapper(this.mapNotNull { it?.wrap() })
        is Map<*, *> -> RuntimeObject.ObjectWrapper(this.entries.associate {
            val (key, value) = it
            if (key !is String)
                throw KoteRuntimeException("<22571494> Unsupported key '$key' in object $this")
            key to value.wrap()
        })

        else -> throw KoteRuntimeException("<13f5b28> Unsupported context item $this")
    }

fun Map<String, Any>.wrapAll(): Map<String, RuntimeObject> = entries.associate {
    it.key to it.value.wrap()
}

class MapContext(
    predefinedRuntimeObjects: Map<String, RuntimeObject>,
    override val renderer: Renderer,
    override val resourceLoader: ResourceLoader
) : RuntimeContext {
    private val runtimeObjects = predefinedRuntimeObjects.toMutableMap()

    override fun get(key: String): RuntimeObject {
        return runtimeObjects[key] ?: throw KoteRuntimeException("<52662afc> Context not found by key '$key'")
    }

    override fun set(key: String, obj: RuntimeObject?) {
        if (obj == null) {
            runtimeObjects.remove(key)
        } else {
            runtimeObjects[key] = obj
        }
    }

    operator fun plus(another: Map<String, RuntimeObject>): RuntimeContext {
        return MapContext(
            (this.runtimeObjects + another).toMutableMap(),
            this.renderer,
            this.resourceLoader
        )
    }
}

fun Map<String, RuntimeObject>.asContext(renderer: Renderer, resourceLoader: ResourceLoader) =
    MapContext(this.toMutableMap(), renderer, resourceLoader)