package dev.limebeck.templateEngine

sealed interface Result<Value, Error> {
    companion object {
        fun <Value> ofSuccess(value: Value) = Success(value)
        fun <Error> ofError(error: Error) = Error(error)
    }

    data class Success<Value>(val value: Value) : Result<Value, Nothing>
    data class Error<Error>(val error: Error) : Result<Nothing, Error>

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Result.Error

    fun getValueOrNull(): Value? {
        return (this as? Success)?.value
    }

    fun getErrorOrNull(): Error? {
        return (this as? Result.Error)?.error
    }

    fun onSuccess(block: Success<Value>.() -> Unit): Result<Value, Error> {
        if (this is Success) {
            this.block()
        }
        return this
    }

    fun <T> onError(block: Result.Error<Error>.() -> Unit): Result<Value, Error> {
        if (this is Result.Error) {
            this.block()
        }
        return this
    }
}