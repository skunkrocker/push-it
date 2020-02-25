package machinehead.model.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

object YAMLParser {
    private val mapper = let {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        mapper
    }

    fun <T : Any> parseTo(fileName: String, dataObject: KClass<T>): T {
        val file = File("src/main/resources/" + fileName)
        return Files.newBufferedReader(Path.of(file.absolutePath))
            .use { mapper.readValue(it, dataObject.java) }
    }
}