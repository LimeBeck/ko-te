package utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

val testScope = MainScope()
actual fun runTest(block: suspend () -> Unit): dynamic = testScope.promise { block() }