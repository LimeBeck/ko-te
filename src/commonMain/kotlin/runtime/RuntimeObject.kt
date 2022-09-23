package dev.limebeck.templateEngine.runtime

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