package dev.limebeck.templateEngine.runtime

sealed interface RuntimeObject {
    class CallableWrapper private constructor(
        internal val parameterNames: List<String>?,
        val block: (args: List<RuntimeObject>, context: RuntimeContext) -> RuntimeObject
    ) : RuntimeObject {
        constructor(block: (args: List<RuntimeObject>, context: RuntimeContext) -> RuntimeObject) : this(null, block)

        companion object {
            fun from(block: (args: List<RuntimeObject>, context: RuntimeContext) -> Any?): CallableWrapper {
                return CallableWrapper { args: List<RuntimeObject>, context: RuntimeContext ->
                    block(args, context).wrap()
                }
            }

            fun from(
                parameterNames: List<String>,
                block: (args: List<RuntimeObject>, context: RuntimeContext) -> Any?
            ): CallableWrapper {
                val names = parameterNames.toList()
                require(names.none { it.isBlank() } && names.toSet().size == names.size) {
                    "Parameter names must be non-blank and unique"
                }
                return CallableWrapper(names) { args, context -> block(args, context).wrap() }
            }
        }
    }

    object Null : RuntimeObject
    object Nothing : RuntimeObject

    class ObjectWrapper(
        val objectMap: Map<String, RuntimeObject>
    ) : RuntimeObject

    class CollectionWrapper(
        val collection: List<RuntimeObject>
    ) : RuntimeObject

    class StringWrapper(
        val string: String
    ) : RuntimeObject

    class NumberWrapper(
        val number: Number
    ) : RuntimeObject

    class BooleanWrapper(
        val value: Boolean
    ) : RuntimeObject
}
