package utils

import kotlinx.coroutines.runBlocking

actual fun runTest(block: suspend () -> Unit): Unit = runBlocking { block() }