package e2e.tests

import e2e.Status

interface Test {
    val name: String
    suspend fun run(): Status
}
