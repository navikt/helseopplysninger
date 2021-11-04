package archive.testutils

fun readResourcesFile(path: String) =
    object {}.javaClass.getResource(path)!!.readBytes()
