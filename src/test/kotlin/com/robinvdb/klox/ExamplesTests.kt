package com.robinvdb.klox

import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.invoke.MethodHandles
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.streams.toList

class ExamplesTests {
    private fun getResourceFiles(path: String): List<String> {
        val resource = MethodHandles.lookup().lookupClass().classLoader.getResource(path)
            ?: error("Resource $path not found")
        FileSystems.newFileSystem(resource.toURI(), emptyMap<String, String>()).use { fs ->
            return Files.walk(fs.getPath(path))
                .filter { it.extension == ".lox" }
                .map { it.absolutePathString() }
                .toList()
        }
    }

    @Test
    fun `run all tests`() {
        val files = getResourceFiles("test/resources")
        files.forEach { print(it) }
    }
}
