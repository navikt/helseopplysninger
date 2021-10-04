package archive.domain

interface Archive {
    suspend fun add(doc: Journalpost)
    suspend fun smokeTest()
}
