package utils

import kotlinx.coroutines.test.TestResult

fun runTest(block: suspend () -> Unit): TestResult = kotlinx.coroutines.test.runTest {
    block()
}
